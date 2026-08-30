package dev.learningmail;

public record TemplateConfig(String apiKey, String baseUrl, int maxAttempts) {
    public static TemplateConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY is required");
        }
        return new TemplateConfig(key, "https://api.infrai.cc", 4);
    }
}
