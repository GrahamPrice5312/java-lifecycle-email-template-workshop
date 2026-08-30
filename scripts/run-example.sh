#!/bin/sh
set -eu
BUILD_DIR="${TMPDIR:-/tmp}/lifecycle-template-workshop"
mkdir -p "$BUILD_DIR"
javac -d "$BUILD_DIR" src/main/java/dev/learningmail/*.java
java -cp "$BUILD_DIR" dev.learningmail.TemplateWorkshop
