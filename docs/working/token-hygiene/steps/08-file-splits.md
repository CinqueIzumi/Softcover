# Step 08 — Large-file splits + size gate (D3, D10)

**Branch:** migration. **One file per session** (08a–08m), then the gate (08n). Run a split only when the
migration branch has no uncommitted migration work, so a split never mixes with a migration stage.
**Delegation:** `softcover-implementer`, one brief per file; `softcover-reviewer` once per file.

## Why

A 2,000+-line file can't be read in one call, so agents page through it and re-read (`LibraryShelf.kt`
was read 37 times). A file must be read before it can be edited, and the reviewer sweeps every touched
file in full. After the split, the layout file lists the sections, `ls section/` is the map, and a change
costs one ~150-line file. A cross-cutting change costs about the same as today, never meaningfully more.

## Files (lines as of 2026-09-20; re-measure before each)

| Sub | File | Lines |
|---|---|---|
| 08a | `feature/book_detail/…/presentation/screen/BookDetailShelf.kt` | 2849 |
| 08b | `feature/profile/…/presentation/screen/ProfileShelf.kt` | 2328 |
| 08c | `feature/library/…/presentation/screen/LibraryShelf.kt` | 1971 |
| 08d | `feature/explore/…/presentation/screen/ExploreShelf.kt` | 1801 |
| 08e | `feature/reading/…/presentation/screen/ReadingShelf.kt` | 1571 |
| 08f | `feature/settings/…/presentation/screen/SettingsShelf.kt` | 1455 |
| 08g | `core/component/…/progress/UpdateProgressBottomSheet.kt` | 1200 |
| 08h | `feature/explore/…/screen/ExploreScreenLayout.mobile.kt` | 994 |
| 08i | `feature/book_detail/…/component/TagEditorBottomSheet.kt` | 795 |
| 08j | `feature/explore/…/screen/ExploreScreenLayout.jvm.kt` | 698 |
| 08k | `feature/settings/…/screen/SettingsScreenLayout.jvm.kt` | 671 |
| 08l | `feature/library/…/screen/LibraryScreenLayout.mobile.kt` | 660 |
| 08m | `feature/reading/…/screen/ReadingScreenLayout.mobile.kt` | 612 |

Where a migration stage has already moved or shrunk a file below 600 lines, skip it and mark it `n/a`.

## Split rules (go into every brief as `## Constraints`)

- **Pure move.** No behaviour, signature or formatting change beyond what the move forces.
- Feature files: one **section** composable per file, together with the private helpers only it uses,
  under the sub-package `…presentation.screen.section`. Helpers shared by several sections →
  `section/<Screen>SectionShared.kt`. The entry file keeps only the composition (the list of section
  calls) and the state wiring.
- `:core:component` files (08g): split along the component's own anatomy (e.g. header / body / footer
  pieces) within its package. Public API unchanged.
- `private` → `internal` only where cross-file use requires it; never `public`.
- File names match the main composable (one-type-per-file + the ktlint rules).
- `@Preview`s go with the composable they preview.
- **No file may exceed 600 lines** after the split; aim for ≤250.

## Brief skeleton (per file)

```
## Goal       Split <file> per the split rules; pure move.
## Files      <file> (N lines) — sections at lines … (grep `^(private |internal )?fun [A-Z]` first)
## Constraints <split rules above>
## Verify     scripts/gradle-quiet.sh :<module>:compileKotlinJvm :<module>:ktlintCheck :<module>:testDebugUnitTest
## Report     ≤150 words: new files with line counts, visibility changes, anything not a pure move
```

Review: `## Scope` = the split commit. The reviewer checks it is a pure move by diffing the concatenated
function bodies (old vs new) and checks visibility changes. Tests: none expected (pure move); only if the
reviewer finds a behaviour change, which would be a defect to revert, not to test.

## 08n — Size gate

- Root `build.gradle.kts`: a `checkPresentationFileSize` task modelled on `checkResourcePackaging`
  (same file, same "gate, not convention" rationale). It fails when any non-test `.kt` file under
  `**/src/*Main/**/presentation/**` or `core/component/src/*Main/**` exceeds **600** lines, listing each
  offender with its count. Wire it into `check`. No baseline or exemption list.
- Document it in `docs/reference/module-structure.md` § Build wiring conventions, next to
  `checkResourcePackaging`, and in `.claude/rules/build-wiring.md`.

## Acceptance (per sub-step / final)

- Per file: the Verify command is green, the reviewer confirms a pure move, and the largest new file is
  ≤600 lines.
- 08n: `scripts/gradle-quiet.sh checkPresentationFileSize` passes; a temporary 601-line file makes it
  fail with the path listed.
- `foundation-upstream.md` FU-9 → "done locally".
