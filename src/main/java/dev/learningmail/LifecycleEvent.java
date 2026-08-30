package dev.learningmail;

public sealed interface LifecycleEvent permits LifecycleEvent.BuildFailed, LifecycleEvent.ReleasePublished {
    String eventId();
    String project();

    record BuildFailed(String eventId, String project, String branch, String diagnostic) implements LifecycleEvent {}
    record ReleasePublished(String eventId, String project, String version, String notesUrl) implements LifecycleEvent {}
}
