---
name: feedback-kotlin-block-comments-nest
description: Writing a literal /* inside a Kotlin /** KDoc */ block comment breaks the build with "Unclosed comment" — Kotlin nests block comments, unlike Java
metadata:
  type: feedback
---

Kotlin's block comments nest (`/* /* inner */ still commented */` is valid), unlike Java's. A KDoc
block that describes comment syntax and writes the literal two characters `/*` — even inside backticks,
even mid-sentence — opens a nested comment the lexer expects to be closed, and `compileKotlin` fails with
`Syntax error: Unclosed comment` pointing at a much later line (the file's true end), not the offending
one.

**Why:** hit this writing KDoc for a helper (`kotlinStyleComment` in `CheckDocBudgetsTask.kt`) that finds
"the first `//` or `/*` in a line" — the literal `` `/*` `` inside the enclosing `/** ... */` opened a
nested comment with no matching close, and the reported error line was the last line of the file, not
the KDoc.

**How to apply:** when a KDoc or block comment needs to talk about Kotlin/Java block-comment syntax,
never write the literal two characters `/*` in it (backticks do not escape it) — describe it in words
("a block-comment opener") instead. Line comments (`//`) do not have this problem and can be written
literally. If `compileKotlin` reports "Unclosed comment" at a line that looks unrelated (often the file's
last line), search backwards for a comment that contains a literal `/*`.
