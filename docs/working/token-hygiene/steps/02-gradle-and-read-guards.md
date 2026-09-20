# Step 02 — Quiet Gradle + large-read guard

**Branch:** tooling. **Depends on:** Step 01 probe answers P1 and P2. **Delegation:** main conversation.

Goal: make the two biggest sources of main-conversation growth — raw Gradle output and paged reads of
huge files — behave the same for everyone, enforced by checked-in hooks rather than by habit.

## 1. `scripts/gradle-quiet.sh`

- Usage: `scripts/gradle-quiet.sh <gradle args…>`. Runs `./gradlew "$@"` from the repo root and writes
  the full output to `build/claude-logs/gradle-<UTC timestamp>.log` (`build/` is already gitignored).
- Prints at most about 40 lines:
  1. `BUILD SUCCESSFUL|FAILED in …` and the task(s) run.
  2. Failing tasks (`> Task … FAILED`, `What went wrong:` + the next 5 lines).
  3. Kotlin compiler errors (`^e: `), deduplicated, capped at 20.
  4. Failing tests (class + method), taken from the `build/test-results/**/TEST-*.xml` files written
     during this run, capped at 20.
  5. ktlint / detekt findings (`file:line:col` lines), capped at 20, plus the total count.
  6. The log path, so the caller can grep it.
- Exit code = Gradle's exit code.
- It must **not** change `JAVA_HOME` or any machine-specific setting; env prefixes set by the caller pass
  through unchanged (the JDK 21 detekt workaround stays the caller's business).

## 2. `.claude/hooks/quiet-gradle.sh` (PreToolUse, matcher `Bash`)

- Runs **after** `block-slow-gradle.sh` in `settings.json`; if that one blocks, this never runs.
- If the command has a `./gradlew` token outside a string, and neither `GRADLE_VERBOSE=1` nor
  `scripts/gradle-quiet.sh` is already in it, rewrite every `./gradlew` token to `scripts/gradle-quiet.sh`,
  keeping env-var prefixes and everything downstream of pipes. Use the same segment-splitting approach
  as `block-slow-gradle.sh`.
- Output: `{"hookSpecificOutput":{"hookEventName":"PreToolUse","updatedInput":{"command":"<rewritten>"}}}`.
  Check that `updatedInput` keeps the other input fields (description, timeout); if it replaces the whole
  input, copy them through.
- Applies in the main conversation and in every agent alike.
- Test cases, run by piping sample JSON into the script, kept in `.claude/hooks/test/quiet-gradle.cases`:
  plain; `JAVA_HOME=… ./gradlew …`; `./gradlew a && ./gradlew b`; `./gradlew x | tail -5`;
  `GRADLE_VERBOSE=1 ./gradlew x` (unchanged); `echo "./gradlew"` (unchanged).

## 3. `.claude/hooks/large-read-guard.sh` (PreToolUse, matcher `Read`)

- Applies to the **main conversation only** (use P1 to detect it).
- If `tool_input.limit` is unset and the file is a text file of more than **600** lines, deny with
  `permissionDecisionReason`:
  `<path> has <N> lines. grep for the symbol and Read with offset/limit, or delegate the work (softcover-implementer / Explore).`
- Images, PDFs, notebooks and files outside the repo pass through.
- 600 matches the D10 size gate: once Step 08 lands, only non-presentation files (DAOs, mappers, tests)
  can trip it.

## 4. Update callers

- `.claude/skills/style-check/SKILL.md`: Gradle calls are rewritten automatically; tell it to read the
  summary and grep the log path instead of re-running verbose.
- Add a two-line note to the migration tracker's "Local verification caveats" (on the migration branch, in
  Step 06): output is summarised, and the full log is at the printed path.

## Acceptance

- Every case in `quiet-gradle.cases` produces the expected rewritten or unchanged command.
- A deliberately failing compile (a temporary edit, then reverted) prints the `e:` line and the log path
  in ≤40 lines.
- A main-conversation `Read` of `BookDetailShelf.kt` without `limit` is denied with the reason; the same
  read with `limit` goes through; a subagent's read goes through.
- `foundation-upstream.md` rows FU-4 and FU-5 → "done locally".
