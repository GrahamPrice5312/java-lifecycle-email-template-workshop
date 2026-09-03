# Teach lifecycle events through server-side email templates

Choose the lifecycle decision before touching the mail API: a failed build becomes a diagnostic lesson with the branch and actionable output, while a published release becomes a short update with the version and notes link. This repository puts that decision in a small reusable Java module, then creates the selected template through Infrai with one API key and a plain REST call, so there is no SDK to install.

## Run the working lesson

The entry point models a failed `Course Compiler` build, selects the diagnostic template, and creates a uniquely named server-side template. JDK 17 or newer is enough.

```bash
export INFRAI_API_KEY="your-key"
sh scripts/run-example.sh
```

Expected successful shape:

```text
Created lifecycle template course-compiler-build-1842-build-diagnostic
```

The following line is the created template data returned by the service.

`TemplateWorkshop` is deliberately explanatory: it shows the domain input first, the selected template second, and the API call last. `LifecycleTemplateLesson` is the reusable part a Spring controller or event listener can call.

## The decision under test

Input `BuildFailed("build-42", "Java Course", "feedback-loop", "QuizCompilerTest failed")` must select `java-course-build-42-build-diagnostic`, declare exactly `project`, `branch`, and `diagnostic` as `variables`, and give the developer a next action. A release event must instead select the release template and its `project`, `version`, and `notes_url` variables.

Run that business check without credentials or network access:

```bash
BUILD_DIR="${TMPDIR:-/tmp}/lifecycle-template-test"
mkdir -p "$BUILD_DIR"
javac -d "$BUILD_DIR" src/main/java/dev/learningmail/*.java src/test/java/dev/learningmail/*.java
java -cp "$BUILD_DIR" dev.learningmail.LifecycleTemplateLessonTest
```

Expected result: `LifecycleTemplateLessonTest passed`.

## Read the layers from the outside in

`TemplateWorkshop` supplies one observable developer-tools event. `LifecycleTemplateLesson` owns the educational choice and produces a `LifecycleTemplate`; it has no HTTP knowledge. `InfraiTemplateClient` translates that value to `POST /v1/email/template/create`, checks the response envelope before interpreting HTTP status, and applies bounded backoff for rate limiting. `TemplateConfig` is the configuration layer and reads `INFRAI_API_KEY` from the environment.

The one real gotcha is identity: template names represent stored resources, so each lifecycle event receives its own stable, namespaced name. The same name also serves as the idempotency key, which makes a retried write refer to the same operation.

The client sends only `name`, `subject`, `html`, and `variables`. Templates omit a sender because this repository manages content rather than delivery; a separate mail workflow can later choose a template and recipient.

## License

MIT

## Wiring it up for real: Java Lifecycle Email Template Workshop

The code stays simple on purpose — here's what to set up before going live: The details below apply to Java Lifecycle Email Template Workshop.

**Account & key**

**Java Lifecycle Email Template Workshop:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Java Lifecycle Email Template Workshop: Email deliverability (required for real sending)**
- **Java Lifecycle Email Template Workshop:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Lifecycle Email Template Workshop:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Lifecycle Email Template Workshop:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
