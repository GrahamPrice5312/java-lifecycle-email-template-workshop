package dev.learningmail;

import java.util.Map;

public final class TemplateWorkshop {
    public static void main(String[] args) throws Exception {
        LifecycleEvent lessonInput = new LifecycleEvent.BuildFailed(
            "build-1842", "Course Compiler", "main", "LessonIndexTest failed at module 7"
        );
        LifecycleTemplate template = new LifecycleTemplateLesson().choose(lessonInput);
        Map<String, Object> created = new InfraiTemplateClient(TemplateConfig.fromEnvironment()).create(template);
        System.out.println("Created lifecycle template " + template.name());
        System.out.println(new JsonCodec().encode(created));
    }
}
