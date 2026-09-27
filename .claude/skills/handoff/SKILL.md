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

   If the old block has a **Due when** line (`- **Due when:** <step> in <tracker> — <condition>`), carry
   it into the new block unchanged. Once the condition holds (check the named step files' checkboxes), drop
   the line, make that step the **Next** item, and start the final message with `Due: <step>.`

   Replace the old block; do not append to it. Drop history the next session does not need — decisions
   belong under the tracker's `## Decisions`, not in `## Now`.

3. **Audit agent memory if a plan step finished.** If `git status --short` shows a deleted step file under a
   plan's `steps/` directory, or this session deleted one itself, read each `.claude/agent-memory/*/MEMORY.md`
   index. Delete the memories that only served that step, and promote the durable ones as the owning agent's
   `## Memory` section directs (`.claude/agents/softcover-{implementer,reviewer,test-writer}.md`); an agent
   with no local definition, such as a plugin agent, keeps only durable, non-obvious lessons. Keep each
   index line in sync with the file it points to. Report what was pruned in the final message.

4. **Summarise uncommitted changes.** Run `git status --short` and group the paths by purpose (e.g.
   "hook scripts under `.claude/hooks/`", "tracker docs") rather than listing every file. Write that under
   **Uncommitted**.

5. **Ask whether to commit.** Propose a single subject-line message (imperative, sentence case, no
   trailing period, no body, no trailers — see `CLAUDE.md` → Commit Messages). Commit only if the user
   says yes; never commit unasked.

6. **End with exactly:** `Handoff written. Type /clear, then ask the next session to resume from the Now block.`
