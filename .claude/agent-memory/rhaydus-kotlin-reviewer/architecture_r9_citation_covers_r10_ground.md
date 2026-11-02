---
name: architecture_r9_citation_covers_r10_ground
description: KDoc citing "off the composition (component-contract.md R9)" for a stored UiState row/model list is an established house idiom, not a misattribution of R10
metadata:
  type: architecture
---

`component-contract.md` R9 ("Mapping happens in the ScreenModel. A composable never calls a mapper") and R10
("A UI model is never built in composition. It arrives on the UiState") are textually distinct, but the
codebase's existing collector/state KDoc consistently cites **R9** — not R10 — for the general claim "this
list/model is assembled off the composition and the render only forwards it" (seen already in
`SelectionMarksCollector`, `DeadlineModelsCollector`, and pre-existing throughout `feature/reading`,
`feature/book_detail`, `feature/explore`). A new file doing the same (e.g. `DisplaySection`'s
"read straight off `displayToggleRows` (built off the composition, R9)") is following that convention
verbatim, not confusing which rule applies.

**Why:** first read, this looks like a KDoc-accuracy bug (R10 is the rule that literally says "UI model never
built in composition"), but the citation is copy-consistent with dozens of pre-existing files, so it is the
project's chosen shorthand, not a per-file error.

**How to apply:** don't flag "off the composition (R9)" as a wrong rule number. Do still flag a citation to a
rule number that doesn't exist, or one that contradicts what the code actually does (e.g. citing R9/R10 next
to a UI model genuinely built inline in the composable).
