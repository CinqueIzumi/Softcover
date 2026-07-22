---
name: feedback_import_order_convention
description: Confirmed import-ordering convention in this project's Kotlin test files (ktlint-enforced project-import ordering rule)
type: feedback
---

Third-party/stdlib imports in test files are sorted fully alphabetically (ASCIIbetical, case-sensitive — uppercase letters sort before lowercase) across the WHOLE import path, not grouped by package prefix beyond what alphabetical order naturally produces.

Confirmed ordering, cross-referenced from `core/profile` test files (e.g. `ProfileRemoteDataSourceImplTest.kt` had it right already):
`io.*` → `kotlin.*` (stdlib, e.g. `kotlin.time.Clock`) → `kotlinx.*` (e.g. `kotlinx.coroutines.*`, `kotlinx.datetime.*`) → `nl.rhaydus.*` (project code) → `org.junit.*`.

Within a single package, uppercase-named imports (classes, e.g. `kotlinx.datetime.LocalDate`) sort before lowercase-named imports (extension functions, e.g. `kotlinx.datetime.minus`), since it's a straight ASCII string sort of the full import statement.

**Why:** CLAUDE.md's on-touch style policy requires fixing pre-existing style violations in any file touched for other reasons — a reviewer flagged a scrambled import block in `RefreshUserProfileDataUseCaseTest.kt` (kotlin/kotlinx interleaved with org.junit) during an unrelated field-rename task, and it had to be fixed in the same pass.

**How to apply:** When touching any Kotlin file for unrelated reasons, do a quick visual scan of the import block; if it's out of ASCIIbetical order, resort it in the same edit rather than leaving it or filing a follow-up.
