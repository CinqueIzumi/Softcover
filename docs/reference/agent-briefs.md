# Agent briefs

Every spawn of a `softcover-*` agent carries a brief with the sections below.
`.claude/hooks/agent-brief-check.sh` refuses a spawn whose brief is missing a required section, and the
agents themselves stop with `BRIEF INCOMPLETE` if one slips through.

A brief is the agent's whole context. Hand it what you already know — exact paths, line numbers, the
contract — so it does not rediscover it. A loose brief on a large file costs 10× a tight one.

## `softcover-implementer`

One agent per vertical slice (state + mapper + render + call sites), so no two agents read the same files.

```markdown
## Goal
What the change does, in one or two sentences, and why.

## Files
- `path/to/File.kt:120-180` — what to change there
- `path/to/Other.kt` — new / call site / read-only reference

## Contract (optional)
The UiState / UiAction / UiEvent shape or the public signature, when it is already decided.

## Constraints (optional)
What must not change; decisions already taken; the design-system section that governs the surface.

## Verify
scripts/gradle-quiet.sh :module:compileKotlinJvm

## Report
≤150 words: Done / Files changed / Verify result / Left over / Needs tests.
```

Required: `## Goal`, `## Files`, `## Verify`, `## Report`.

## `softcover-test-writer`

```markdown
## Files
- `path/to/UseCase.kt` — the unit under test
- `path/to/UseCaseTest.kt:40-95` — the construction sites to fix / where to add

## Verify
scripts/gradle-quiet.sh :module:testAndroidHostTest --tests "nl.rhaydus.softcover.feature.x.YTest"

## Report
≤150 words: tests added / pass or fail with names and diagnosis.
```

Required: `## Files`, `## Verify`, `## Report`.

Scoping for small mechanical changes (one field, a rename, compile breaks):
- Do the `grep` yourself and paste the construction sites with line numbers under `## Files`.
- List the one or two round-trip tests you want; skip the package-wide audit.
- Give exactly one narrow `--tests` filter; say "do not run the broader suite, do not re-audit".

When the target is a whole package or directory, add under `## Files`: "audit existing test files in the
target for coverage gaps and close them in the same pass." For independent files, spawn test-writers in
parallel on disjoint file sets.

## `softcover-reviewer`

```markdown
## Scope
Commit range (`main..HEAD`, `abc123..def456`), `staged`, `unstaged`, or a file list.

## Gates (optional)
The gate result you already have, e.g. "`./gradlew styleCheck` passed".

## Verify (optional)
One narrow command the reviewer may run if no gate result is given.

## Focus (optional)
What the change is meant to do; areas you want looked at hardest.
```

Required: `## Scope`.
