package dev.learningmail;

import java.util.List;

public record LifecycleTemplate(String name, String subject, String html, List<String> templateVars) {}
