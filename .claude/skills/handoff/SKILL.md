---
name: handoff
description: Rewrite the active tracker's `## Now` block so the next session can resume from it after /clear, then ask before committing.
disable-model-invocation: true
---

# Handoff

Close out this session so a fresh one can pick up from the tracker alone. The next session starts from
the `## Now` block of the tracker listed in `docs/working/ACTIVE.md`, so that block must carry everything
it needs.

## Procedure

1. **Resolve the tracker.** Read `docs/working/ACTIVE.md` (one repo-relative path per line). If it lists
   one file, use it. If it lists several and this session touched more than one of them, ask the user
   which to hand off. If it is missing or empty, tell the user and stop.

2. **Rewrite its `## Now` block** (everything from `## Now` up to the next `## ` heading) to this shape,
   **at most 40 lines**:

   ```markdown
   ## Now

   - **State:** what is done, in one or two sentences. Name finished steps/sections, not the journey.
   - **Next:** the exact next action, with repo-relative file references (the step file and section,
     e.g. `steps/02-gradle-and-read-guards.md` § 3). One item, not a list of options.
   - **Open questions:** anything waiting on the user, or "none".
   - **Verification:** the last gate that ran and its result (e.g. "`./gradlew styleCheck` passed"), or
     "not run" with the reason.
   - **Uncommitted:** a summary of `git status --short` (step 3).
   ```

   Replace the old block; do not append to it. Drop history the next session does not need — decisions
   belong under the tracker's `## Decisions`, not in `## Now`.

3. **Summarise uncommitted changes.** Run `git status --short` and group the paths by purpose (e.g.
   "hook scripts under `.claude/hooks/`", "tracker docs") rather than listing every file. Write that under
   **Uncommitted**.

4. **Ask whether to commit.** Propose a single subject-line message (imperative, sentence case, no
   trailing period, no body, no trailers — see `CLAUDE.md` → Commit Messages). Commit only if the user
   says yes; never commit unasked.

5. **End with exactly:** `Handoff written. Type /clear, then ask the next session to resume from the Now block.`
