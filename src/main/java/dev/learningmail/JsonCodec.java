package dev.learningmail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonCodec {
    public String encode(Object value) {
        if (value == null) return "null";
        if (value instanceof String text) return "\"" + escape(text) + "\"";
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof Map<?, ?> map) {
            List<String> entries = new ArrayList<>();
            map.forEach((key, item) -> entries.add(encode(key.toString()) + ":" + encode(item)));
            return "{" + String.join(",", entries) + "}";
        }
        if (value instanceof Iterable<?> items) {
            List<String> encoded = new ArrayList<>();
            items.forEach(item -> encoded.add(encode(item)));
            return "[" + String.join(",", encoded) + "]";
        }
        throw new IllegalArgumentException("Cannot encode " + value.getClass().getName());
    }

    public Map<String, Object> decodeObject(String json) {
        Object value = new Parser(json).parse();
        if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException("Expected a JSON object");
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> result.put(key.toString(), item));
        return result;
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static final class Parser {
        private final String source;
        private int index;
        Parser(String source) { this.source = source; }

        Object parse() {
            Object value = value();
            whitespace();
            if (index != source.length()) throw new IllegalArgumentException("Unexpected JSON suffix");
            return value;
        }

        private Object value() {
            whitespace();
            if (peek('{')) return object();
            if (peek('[')) return array();
            if (peek('"')) return string();
            if (source.startsWith("true", index)) { index += 4; return true; }
            if (source.startsWith("false", index)) { index += 5; return false; }
            if (source.startsWith("null", index)) { index += 4; return null; }
            return number();
        }

        private Map<String, Object> object() {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            whitespace();
            if (take('}')) return result;
            do {
                String key = string();
                whitespace(); expect(':');
                result.put(key, value());
                whitespace();
            } while (take(','));
            expect('}');
            return result;
        }

        private List<Object> array() {
            expect('[');
            List<Object> result = new ArrayList<>();
            whitespace();
            if (take(']')) return result;
            do { result.add(value()); whitespace(); } while (take(','));
            expect(']');
            return result;
        }

        private String string() {
            whitespace(); expect('"');
            StringBuilder result = new StringBuilder();
            while (index < source.length()) {
                char c = source.charAt(index++);
                if (c == '"') return result.toString();
                if (c != '\\') { result.append(c); continue; }
                char escaped = source.charAt(index++);
                if (escaped == 'n') result.append('\n');
                else if (escaped == 'r') result.append('\r');
                else if (escaped == 't') result.append('\t');
                else if (escaped == 'u') {
                    result.append((char) Integer.parseInt(source.substring(index, index + 4), 16));
                    index += 4;
                } else result.append(escaped);
            }
            throw new IllegalArgumentException("Unterminated JSON string");
        }

        private Number number() {
            int start = index;
            while (index < source.length() && "-+0123456789.eE".indexOf(source.charAt(index)) >= 0) index++;
            String token = source.substring(start, index);
            return token.contains(".") || token.contains("e") || token.contains("E")
                ? Double.parseDouble(token) : Long.parseLong(token);
        }

        private void whitespace() { while (index < source.length() && Character.isWhitespace(source.charAt(index))) index++; }
        private boolean peek(char c) { whitespace(); return index < source.length() && source.charAt(index) == c; }
        private boolean take(char c) { if (peek(c)) { index++; return true; } return false; }
        private void expect(char c) { if (!take(c)) throw new IllegalArgumentException("Expected " + c); }
    }
}
