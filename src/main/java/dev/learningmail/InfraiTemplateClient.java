package dev.learningmail;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InfraiTemplateClient {
    private final TemplateConfig config;
    private final HttpClient http;
    private final JsonCodec json;

    public InfraiTemplateClient(TemplateConfig config) {
        this(config, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), new JsonCodec());
    }

    InfraiTemplateClient(TemplateConfig config, HttpClient http, JsonCodec json) {
        this.config = config;
        this.http = http;
        this.json = json;
    }

    public Map<String, Object> create(LifecycleTemplate template) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", template.name());
        body.put("subject", template.subject());
        body.put("html", template.html());
        body.put("variables", template.templateVars());
        return postTemplate(body, template.name());
    }

    private Map<String, Object> postTemplate(Map<String, Object> body, String operationKey)
        throws IOException, InterruptedException {
        // Canonical call: POST /v1/email/template/create
        long delayMillis = 250;
        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.baseUrl() + "/v1/email/template/create"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", operationKey)
                .method("POST", HttpRequest.BodyPublishers.ofString(json.encode(body)))
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> envelope = json.decodeObject(response.body());
            if (response.statusCode() == 429 && attempt < config.maxAttempts()) {
                Thread.sleep(retryDelay(response, delayMillis));
                delayMillis *= 2;
                continue;
            }
            if (!Boolean.TRUE.equals(envelope.get("ok"))) {
                throw InfraiException.from(envelope, response.statusCode());
            }
            if (response.statusCode() >= 500) throw new IOException("Template service returned HTTP " + response.statusCode());
            Object data = envelope.get("data");
            if (!(data instanceof Map<?, ?> map)) throw new IOException("Template response data is not an object");
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, value) -> result.put(key.toString(), value));
            return result;
        }
        throw new IOException("Template request exhausted its retry policy");
    }

    private long retryDelay(HttpResponse<?> response, long fallback) {
        return response.headers().firstValue("Retry-After")
            .map(value -> { try { return Long.parseLong(value) * 1000; } catch (NumberFormatException ignored) { return fallback; } })
            .orElse(fallback);
    }

    public static final class InfraiException extends RuntimeException {
        private final int status;
        private final Map<String, Object> detail;
        private InfraiException(String code, Map<String, Object> detail, int status) {
            super(code + ": " + detail);
            this.status = status;
            this.detail = Map.copyOf(detail);
        }
        static InfraiException from(Map<String, Object> envelope, int status) {
            Object raw = envelope.get("error");
            Map<String, Object> detail = new LinkedHashMap<>();
            if (raw instanceof Map<?, ?> map) map.forEach((key, value) -> detail.put(key.toString(), value));
            return new InfraiException(String.valueOf(detail.getOrDefault("code", "REQUEST_REJECTED")), detail, status);
        }
        public int status() { return status; }
        public Map<String, Object> detail() { return detail; }
    }
}
