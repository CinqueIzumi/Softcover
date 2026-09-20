# Step 01 — Session loop + hook probe

**Branch:** tooling. **Delegation:** main conversation (small scripts that need live checking).

Goal: keep sessions to one step each without relying on anyone remembering to. The model can't run
`/clear`; only the user can. The loop is therefore: the context-budget warning → `/handoff` → the user
types `/clear` → the SessionStart hook injects the next step.

## 1. Hook probe (do first; the later steps depend on it)

Settle, by experiment, what the docs left contradictory or unclear. Add a temporary
`.claude/hooks/_probe.sh` that appends its stdin JSON to `$CLAUDE_PROJECT_DIR/build/claude-probe.jsonl`,
registered for `PreToolUse` (matchers `Agent`, `Bash`, `Read`) and `SessionStart`. Then:

| Question | How to settle it |
|---|---|
| P1: are `agent_id` / `agent_type` absent in main-conversation calls and present in subagent calls? | One main `Bash` call, plus one Explore agent that runs one `Bash` and one `Read`; compare the logged JSON |
| P2: is `tool_name` for spawns `"Agent"`, and are `tool_input.subagent_type` / `tool_input.prompt` present? | Same Explore spawn |
| P3: do `.claude/rules/*.md` files with `paths:` load inside subagents? | A temporary rule `paths: ["build/probe/**"]` containing a unique marker word; a subagent reads `build/probe/x.txt` and says whether it sees the marker in its instructions |
| P4: what does `autoCompactWindow` mean (a percentage? tokens?) | settings docs + `/config`; record the value that makes compaction trigger at about 250K on the 1M model |

Record the answers under `README.md` → `## Decisions` as **P1–P4**, then delete the probe hook and the
probe rule. Steps 02–04 cite these answers.

## 2. Active-work pointer

- New `docs/working/ACTIVE.md`: one repo-relative path per line, each a tracker with a `## Now` block. On
  the tooling branch: `docs/working/token-hygiene/README.md`.
- Convention (written into `CLAUDE.md` in Step 04): every working tracker keeps a `## Now` block of at
  most 40 lines covering State, Next (with exact file references), Open questions, Verification (the last
  gate result), and Uncommitted changes.

## 3. `.claude/hooks/session-resume.sh` (SessionStart, matcher `startup|clear|compact`)

- Read `docs/working/ACTIVE.md`. For each listed file, pull out its `## Now` section (up to the next
  `## ` heading), capped at 60 lines; past the cap, append a note that the block is too long.
- Emit `{"hookSpecificOutput":{"hookEventName":"SessionStart","additionalContext":"<header + blocks>"}}`
  with the header `Active work (docs/working/ACTIVE.md). Read only the step file the Next line names.`
- If `ACTIVE.md` is missing or empty, output nothing and exit 0. Needs only `bash`, `sed` and `jq` (the
  existing hook already requires `jq`).

## 4. `.claude/hooks/context-budget.sh` (UserPromptSubmit)

- Read `transcript_path` from stdin and find the last assistant entry that has `usage`. Its context is
  `input_tokens + cache_creation_input_tokens + cache_read_input_tokens`.
- Threshold: `SOFTCOVER_CONTEXT_BUDGET`, default `150000`. At or over it, emit `additionalContext`:
  `Context is ~<N>K tokens (budget <B>K). Finish the current step only, then ask the user to run /handoff and /clear.`
- Silent below the threshold. Always exit 0, and never block the prompt.

## 5. `/handoff` skill — `.claude/skills/handoff/SKILL.md`

- Frontmatter: `name: handoff`, a one-line description, `disable-model-invocation: true` (only the user
  starts it).
- Procedure:
  1. Resolve the tracker from `ACTIVE.md`; if several are listed and the session touched more than one,
     ask which.
  2. Rewrite its `## Now` block to the convention (≤40 lines).
  3. Summarise `git status --short` under Uncommitted changes.
  4. **Ask** whether to commit (subject-only message); never commit unasked.
  5. End with: `Handoff written. Type /clear — the next session resumes from the Now block.`

## 6. Settings (`.claude/settings.json`, checked in)

- Register `session-resume.sh` (SessionStart) and `context-budget.sh` (UserPromptSubmit) next to the
  existing `block-slow-gradle.sh`.
- Set `autoCompactWindow` to the value from P4, as a backstop only.

## 7. `scripts/claude/token-usage.py`

Check in a cleaned-up version of the analysis script used for the baseline. It reads
`~/.claude/projects/<this-project>/**/*.jsonl` and prints, per session: turns, mean/p90/max context,
compaction count, subagent runs by type with cost per run, and the most-read files. Needs no
dependencies beyond the Python 3 standard library. Step 09 uses it.

## Acceptance

- P1–P4 are recorded in the README, and the probe hook and probe rule are removed.
- In a fresh session, `/clear` makes the Now block appear with no prompt from the user (check on the next
  step's start).
- With `SOFTCOVER_CONTEXT_BUDGET=1000` exported, one prompt produces the budget notice.
- `/handoff` rewrites the Now block and asks before committing.
- `foundation-upstream.md` rows FU-3 and FU-11 → "done locally".
