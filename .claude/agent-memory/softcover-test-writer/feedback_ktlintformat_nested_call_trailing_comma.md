---
name: feedback-ktlintformat-nested-call-trailing-comma
description: ktlintFormat can mis-wrap a nested call passed directly as the sole argument of another call, leaving a malformed `),)` — hand-fix by wrapping the outer call too
metadata:
  type: feedback
---

`ktlintFormat` (rhaydus:multi-arg-wrapping) does not always correctly reformat a construct like
`outer.set(inner(arg1, arg2))` when `inner`'s args get wrapped one-per-line with trailing commas. Observed
output: `outer.set(inner(\n arg1,\n arg2,\n),)` — a stray `),)` on the closing line, which does not compile
cleanly / reads as broken.

**Why:** hit this in `build-logic/src/test/kotlin/CheckDocBudgetsTaskTest.kt` after running `ktlintFormat` to
clear `rhaydus:multi-arg-wrapping` violations — one call site (`task.activeFile.set(write(...))`) came out
malformed and needed a manual second wrap.

**How to apply:** after `ktlintFormat`, grep the touched file for `),)` (or any `)` immediately followed by
`,)`) before considering ktlint-only fixups done. Fix by wrapping the outer call onto its own lines too:
```kotlin
outer.set(
    inner(
        arg1,
        arg2,
    ),
)
```
Re-run `ktlintCheck` after the manual fix to confirm it's accepted. This is a formatter quirk, not a rule to
memorize — just a spot-check worth doing whenever `ktlintFormat` touches nested call sites.
