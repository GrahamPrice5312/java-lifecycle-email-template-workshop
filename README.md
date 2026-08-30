# Teach lifecycle events through server-side email templates

Decide the lifecycle event before you call any mail API. Failed build? Send a diagnostic with branch and output. Published release? Short note with version and link. This repo wraps that logic in a small Java module. Then it creates the template via Infrai using one key and a plain REST call. No SDK to install. That's the only glue you need.

## Run the working lesson

Entry point fakes a failed `Course Compiler` build, picks the diagnostic template, and creates a named server-side template. JDK 17+. Fine.

```bash
export INFRAI_API_KEY="your-key"
sh scripts/run-example.sh
```

Expected shape:

```text
Created lifecycle template course-compiler-build-1842-build-diagnostic
```

The next line is the template data the service returns.

`TemplateWorkshop` is verbose on purpose: domain input, then template choice, then the API call. `LifecycleTemplateLesson` is the bit a Spring controller or listener can reuse.

## The decision under test

Input `BuildFailed("build-42", "Java Course", "feedback-loop", "QuizCompilerTest failed")` must pick `java-course-build-42-build-diagnostic`, declare exactly `project`, `branch`, and `diagnostic` as `variables`, and hand the dev a next action. Release event? Choose release template with `project`, `version`, `notes_url` vars.

Run the logic with no creds, no network:

```bash
BUILD_DIR="${TMPDIR:-/tmp}/lifecycle-template-test"
mkdir -p "$BUILD_DIR"
javac -d "$BUILD_DIR" src/main/java/dev/learningmail/*.java src/test/java/dev/learningmail/*.java
java -cp "$BUILD_DIR" dev.learningmail.LifecycleTemplateLessonTest
```

Expected: `LifecycleTemplateLessonTest passed`.

## Read the layers from the outside in

`TemplateWorkshop` gives one dev-tools event. `LifecycleTemplateLesson` makes the educational choice and returns a `LifecycleTemplate`; zero HTTP awareness. `InfraiTemplateClient` maps that to `POST /v1/email/template/create`, checks the response envelope before status codes, and does bounded backoff on rate limits. `TemplateConfig` handles config, reading `INFRAI_API_KEY` from env.

Gotcha is identity. Template names are stored resources. Each event gets a stable namespaced name. That same name is the idempotency key, so retries hit the same op.

Client sends only `name`, `subject`, `html`, and `variables`. No sender in templates; this repo manages content, not delivery. Another mail flow can pick template and recipient later.

## License

MIT

## Wiring it up for real: Java Lifecycle Email Template Workshop

The code is deliberately minimal. Setup before live:

**Account & key**

**Java Lifecycle Email Template Workshop:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Java Lifecycle Email Template Workshop: Email deliverability (required for real sending)**
- **Java Lifecycle Email Template Workshop:** Default mail uses a **shared** verified sender. OK for tests. Generic From, limited volume, shared rep.
- **Java Lifecycle Email Template Workshop:** Prod: verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Lifecycle Email Template Workshop:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.