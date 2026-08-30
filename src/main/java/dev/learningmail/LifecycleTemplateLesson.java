package dev.learningmail;

import java.util.List;

public final class LifecycleTemplateLesson {
    public LifecycleTemplate choose(LifecycleEvent event) {
        String namespace = slug(event.project()) + "-" + slug(event.eventId());
        if (event instanceof LifecycleEvent.BuildFailed failed) {
            return new LifecycleTemplate(
                namespace + "-build-diagnostic",
                "Build needs attention: {{project}} on {{branch}}",
                "<h1>Build diagnostic</h1><p>{{project}} on <strong>{{branch}}</strong> needs attention.</p>"
                    + "<pre>{{diagnostic}}</pre><p>Read the diagnostic, make one change, then run the build again.</p>",
                List.of("project", "branch", "diagnostic")
            );
        }
        LifecycleEvent.ReleasePublished release = (LifecycleEvent.ReleasePublished) event;
        return new LifecycleTemplate(
            namespace + "-release-published",
            "{{project}} {{version}} is ready to study",
            "<h1>{{project}} {{version}}</h1><p>The release is published.</p>"
                + "<p><a href=\"{{notes_url}}\">Read the release notes</a> before updating your course project.</p>",
            List.of("project", "version", "notes_url")
        );
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
