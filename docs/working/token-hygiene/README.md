# Token hygiene — implementation plan

> **Lifecycle.** Working plan for one rollout. Delete this directory (except
> [`foundation-upstream.md`](foundation-upstream.md), which moves to `docs/working/`) in the PR that
> lands the last step.

## Now

- **State:** Steps 00 and 01 done and their step files deleted. Step 01 delivered `docs/working/ACTIVE.md`,
  `.claude/hooks/context-budget.sh` (UserPromptSubmit), the `/handoff` skill and
  `scripts/claude/token-usage.py`; the SessionStart resume hook was dropped (D13).
- **Next:** Step 02, `steps/02-gradle-and-read-guards.md` from § 1.
- **Open questions:** none.
- **Verification:** the budget notice fired live with `SOFTCOVER_CONTEXT_BUDGET=1000 claude` and stayed
  silent at the 150K default; `/handoff` ran and asked before committing.
- **Uncommitted:** Steps 00/01 close-out in the tracker docs: this block and the Steps table in
  `README.md`, plus the deleted `steps/00-branch-setup.md` and `steps/01-session-loop.md`.

## How to run a step

1. Start a fresh session (`/clear`) and ask it to resume from this `## Now` block.
2. Read **this file's `## Now` block and the one step file it names**. Do not read the other step
   files, and do not read `docs/working/component-library-migration.md` unless the step says so.
3. Every step ends the same way: the step's acceptance checks pass → update `## Now` → tick the step
   below → update its row in [`foundation-upstream.md`](foundation-upstream.md) if it has one → delete
   the step's file under `steps/` (it is no longer needed once the step is fully done) and unlink its
   row below → **ask the user before committing** (subject-only message) → the user types `/clear`.

## Steps

| # | Step | Branch | Status |
|---|---|---|---|
| 00 | Branch setup | — | [x] |
| 01 | Session loop + hook probe | tooling | [x] |
| 02 | [Quiet Gradle + large-read guard](steps/02-gradle-and-read-guards.md) | tooling | [ ] |
| 03 | [Local agents, deny rules, brief hook, memory triage](steps/03-agents.md) | tooling | [ ] |
| 04 | [CLAUDE.md catalogue + path-scoped rules](steps/04-claude-md.md) | tooling | [ ] |
| 04b | [Doc-growth guards](steps/04b-doc-growth-guards.md) | tooling | [ ] |
| 05 | [Land the tooling branch](steps/05-land-tooling.md) | tooling → main → migration | [ ] |
| 06 | [Migration tracker cleanup](steps/06-tracker.md) | migration | [ ] |
| 07 | [Design-system docs restructure](steps/07-design-docs.md) (07a–07d) | migration | [ ] |
| 08 | [Large-file splits + size gate](steps/08-file-splits.md) (08a–08n) | migration | [ ] |
| 09 | [Measure and close out](steps/09-measure.md) | migration | [ ] |

`tooling` = a new branch off `main` (name chosen in Step 00). `migration` =
`275-migrate-every-component-into-a-corecomponent-library-driven-by-ui-models`.

## Decisions (from the user, 2026-09-20)

- **D1** Tracker outcome write-ups (§ 5b–5n): delete once any durable rule is confirmed to be in
  `component-contract.md`.
- **D2** Component docs: KDoc on the UI model for components already in `:core:component`, plus a
  one-line index. Families not yet migrated get a per-family file that the family's migration stage
  converts into KDoc and deletes. `patterns.md` is split per surface.
- **D3** Split the large presentation files now, in place; migration stages move whole files later.
- **D4** Keep agents; clone them locally under new names (`softcover-implementer`, `softcover-reviewer`,
  `softcover-test-writer`); deny the plugin originals in this project. The foundation can't be updated
  right now, so every change is also recorded in [`foundation-upstream.md`](foundation-upstream.md).
- **D5** Reviewer memory: keep learnings; promote per-review rules into docs or gates; delete snapshots of
  past stages.
- **D6** `CLAUDE.md` becomes a catalogue; the managed Rhaydus block is overwritten locally with the
  proposed foundation version.
- **D7** Session loop: a `/handoff` skill, checked into the repo. (The SessionStart resume hook was
  dropped; see D13.)
- **D8** Small changes (≤3 existing files, no new file, no public API change) are done in the main
  conversation; anything bigger goes to an agent with a structured brief.
- **D9** Branches: tooling on a new branch off `main`; tracker, docs and splits on the migration branch.
- **D11** Doc-growth guards (Step 04b) approved; files already over budget follow a **ratchet** (may
  shrink, never grow) until Steps 06–07 bring them under budget.
- **D12** Tooling branch: `claude-token-hygiene-tooling`.
- **P1** (probe, 2026-09-20) Main-conversation hook input has no `agent_id` / `agent_type`. Subagent calls
  carry `agent_id`; `agent_type` is usually set (`"Explore"`) but was `null` on one harness-internal
  agent. Hooks tell a subagent apart by `agent_id` presence, never by `agent_type`.
- **P2** Spawns log as `tool_name: "Agent"` with `tool_input.{description, prompt, subagent_type}`.
  `PreToolUse` fires before the auto-mode check and before other hooks' refusals, so a denied or
  re-issued call is logged too (hooks must not assume one entry per real spawn).
- **P3** `.claude/rules/*.md` with `paths:` load inside subagents: the rule appeared in an Explore agent's
  context right after it read a matching file.
- **P4** `/config` exposes auto-compact only as a boolean (`true`); `autoCompactWindow`'s unit could not be
  confirmed, so Step 01 did **not** set it. The context-budget hook is the only budget backstop.
- **P5** Hook edits to `.claude/settings.json` take effect mid-session (no restart needed). In auto mode the
  classifier refuses the model's own edits to `.claude/settings.json` and its reads of hook logs and
  `~/.claude/projects/` transcripts; the user applies those via `!` commands.
- **D13** (2026-09-20) SessionStart resume hook removed. It did inject the Now block after `/clear`, but a
  hook can only add context, not start a turn, so the session still waited for a prompt. The user asks
  the fresh session to resume instead.
- **D10** Size gate: 600 lines for presentation and `:core:component` main sources, no baseline, added
  after the splits.

## Baseline (measured 2026-09-20, 12 sessions / 109 subagent runs)

Step 09 re-measures against these numbers with `scripts/claude/token-usage.py` (added in Step 01).

| Metric | Baseline |
|---|---|
| Main conversation: turns per working session | 156–604 |
| Main conversation: mean context per turn | 182K–424K |
| Main conversation: final context | 280K–710K |
| Subagent cost per run — rhaydus-logic / rhaydus-ui / code-reviewer | 14.6M / 14.7M / 13.5M |
| Subagent cost per run — unit-test-writer (tight briefs) | 1.9M |
| Subagent turns per run — logic / ui / reviewer | 105 / 101 / 99 |
| Subagent starting context (custom agents vs Explore) | ~48K vs ~11K |
| `CLAUDE.md` | 7.3K tokens |
| Migration tracker | 174KB (~45K tokens), read 55× |
| `components.md` / `patterns.md` | 99KB / 88KB, each a single section |
| Presentation / `:core:component` files over 600 lines | 13 |
