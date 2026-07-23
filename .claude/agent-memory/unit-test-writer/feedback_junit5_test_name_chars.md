---
name: junit5-test-name-illegal-chars
description: JUnit 5 backtick test names must not contain colons — use em-dash or plain words instead
metadata:
  type: feedback
---

Kotlin JUnit 5 backtick test names with `:` cause a compile-time "illegal characters" error.

**Why:** The Kotlin compiler rejects `:` inside backtick identifiers at the JVM level.

**How to apply:** Replace `fun \`foo: bar\`` with `fun \`foo — bar\`` (em-dash) or rephrase to avoid the colon entirely. Also applies to any other JVM-illegal characters (e.g. `<`, `>`, `/`, `.`, `[`, `]`) — confirmed `/` also triggers "Name contains illegal characters" (e.g. writing "mode/direction" in a test name). When trimming an over-140-char backtick test name, prefer plain words ("and"/"or") over slashes or extra punctuation.
