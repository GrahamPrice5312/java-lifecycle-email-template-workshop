package dev.learningmail;

import java.util.List;

public final class LifecycleTemplateLessonTest {
    public static void main(String[] args) {
        LifecycleTemplateLesson lesson = new LifecycleTemplateLesson();
        LifecycleTemplate failed = lesson.choose(new LifecycleEvent.BuildFailed(
            "build-42", "Java Course", "feedback-loop", "QuizCompilerTest failed"
        ));
        check(failed.name().equals("java-course-build-42-build-diagnostic"), "namespaced build template");
        check(failed.templateVars().equals(List.of("project", "branch", "diagnostic")), "diagnostic variables");
        check(failed.html().contains("run the build again"), "developer action");

        LifecycleTemplate released = lesson.choose(new LifecycleEvent.ReleasePublished(
            "release-9", "Java Course", "2.1.0", "https://example.test/releases/2.1.0"
        ));
        check(released.name().endsWith("release-published"), "release decision");
        check(released.templateVars().equals(List.of("project", "version", "notes_url")), "release variables");
        System.out.println("LifecycleTemplateLessonTest passed");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
