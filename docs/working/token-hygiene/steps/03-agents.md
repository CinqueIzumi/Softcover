# Step 03 — Local agents, deny rules, brief hook, memory triage

**Branch:** tooling. **Depends on:** P1–P3. **Delegation:** main conversation writes the agent files (they
are orchestration config, not code). Memory triage may go to one `general-purpose` agent with the
criteria below, as long as its report is a keep / promote / delete table under 200 words.

## Why

Measured: open-ended briefs and the logic → UI split cost 14–15M tokens per run (100+ turns); the tightly
briefed test-writer costs 1.9M (24 turns). Each custom agent starts at ~48K tokens. The fix is tighter
contracts, not fewer agents (D4).

## 1. Source material

Read these; they are the originals being replaced:
- `~/.claude/plugins/cache/rhaydus/rhaydus-kotlin/0.3.1/agents/{rhaydus-logic,rhaydus-ui,code-reviewer,unit-test-writer}.md`
- `.claude/agents/unit-test-writer.md` (the local duplicate; its token-aware rules are the good part)

## 2. `.claude/agents/softcover-implementer.md`

Replaces rhaydus-logic and rhaydus-ui: **one agent per vertical slice** (state + mapper + render + call
sites), so no two agents rediscover the same files.

- Frontmatter: `tools: Read, Edit, Write, Bash, Grep, Glob`; `model: sonnet`; `memory: project`;
  `maxTurns: 80` (a safety net against runaway runs, not a target).
- Instructions (aim for ≤120 lines):
  - **Brief gate:** if the brief is missing `## Goal`, `## Files` or `## Verify`, reply
    `BRIEF INCOMPLETE: <missing>` and stop.
  - **Reading discipline:** start from the files in `## Files`; grep before reading; read files over
    about 300 lines in windows; never re-read a file you already hold unless you edited it.
  - **Where the rules live:** a short map, not the content — TOAD → `docs/rhaydus/0.3.1/toad-architecture.md`
    § add-a-feature; component contract → `docs/reference/design-system/component-contract.md` rules
    block; design system → the index `docs/reference/design-system.md`, then one section file; style →
    enforced by `ktlint`/`detekt`, plus the review-only rules in `docs/reference/code-style.md`. If P3
    showed path-scoped rules load in subagents, say so and cut this map down accordingly.
  - **Reuse first:** check `docs/rhaydus/0.3.1/CAPABILITIES.md` **only** when about to hand-roll a
    component, modifier or util.
  - **Boundaries:** never write tests (say what should be tested); never commit; never spawn agents.
  - **Documentation:** comments and KDoc follow the `document-code` skill standard.
  - **Budget:** past about 40 tool calls, stop and report what's done and what's left instead of pushing on.
  - **Verify:** run exactly the `## Verify` command (`scripts/gradle-quiet.sh …`); on failure, fix and
    re-run at most twice, then report.
  - **Report:** ≤150 words, as `Done / Files changed / Verify result / Left over / Needs tests`.

## 3. `.claude/agents/softcover-reviewer.md`

- Frontmatter: `tools: Read, Grep, Glob, Bash, Edit, Write` (Edit/Write **only** for its memory directory,
  and the instructions say so); `model: sonnet`; `memory: project`; `maxTurns: 60`.
- Differences from the plugin reviewer:
  - **No** "always read `CLAUDE.md` and the code-style guide". `CLAUDE.md` is already loaded, and the
    mechanical style is gated by ktlint/detekt. It reads **only** the review-only section of
    `docs/reference/code-style.md` (created in §5 below) plus whatever the brief's `## Scope` requires.
  - The on-touch full-file sweep stays (it is normative), limited to the review-only rules, plus
    correctness and architecture findings in the changed hunks and anything they reach.
  - Gates: the reviewer does not run `styleCheck`. The brief states the gate result; if it is absent, the
    reviewer runs the one narrow command given in `## Verify`, or none.
  - One review per stage: it does not ask for second-opinion forks.
  - Output format: keep the plugin's 🔴/🟡/🔵 structure; ≤400 words unless there are more than 8 findings.
- Brief gate: requires `## Scope` (commit range or file list).

## 4. `.claude/agents/softcover-test-writer.md`

- A merge of the local and plugin test-writers: keep the local file's token-aware rules (grep large test
  files, narrow `--tests` filter, log-and-grep over-long Gradle output); take the plugin's
  project-agnostic structure; drop anything that duplicates `CLAUDE.md` § Test Writing.
- Frontmatter: `tools: Read, Edit, Write, Bash, Grep, Glob`; `model: sonnet`; `memory: project`;
  `maxTurns: 60`.
- Brief gate: requires `## Files` and `## Verify`.
- Keeps the `CLAUDE.md` rule: on any failing test, report the names and the diagnosis verbatim; no fix
  round.

## 5. Memory triage (D5)

Criteria:
- **Keep:** a pattern that will recur and that the code doesn't make obvious (a race, a false-positive
  trap, a contract a component doesn't enforce).
- **Promote:** a rule applied on every review → add it to the **review-only** section of
  `docs/reference/code-style.md` (create the section if missing), or to a ktlint/detekt rule when it can
  be mechanized; then delete the memory.
- **Delete:** a snapshot of one past review or stage whose facts are now in the code or docs, or a note
  that is stale.

Starting sort for `.claude/agent-memory/rhaydus-kotlin-code-reviewer/` (verify each before acting):
- Keep: `architecture_toad_init_side_effects`, `project_toad_action_race_untracked_job`,
  `feedback_onstart_stateflow_double_emit`, `project_adaptive_modal_sheet_scroll_contract`,
  `architecture_full_height_sheet_pinning`, `architecture_foundation_press_modifiers`,
  `project_skeleton_crossfade_contract`, `project_skeleton_dimension_math`, `project_bottom_chrome_padding`,
  `project_kmp_patterns`, `project_design_system_doc_drift_pattern`, `feedback_staged_vs_unstaged_review_scope`,
  `project_local_tag_cache_10_16` (the `UserTag.count` overload), `project_profile_redesign_refresh_gate_race`,
  `project_because_you_read_reselect_stuck_loading`.
- Promote, then delete: `style_trailing_lambda_glomming_recurs`, `project_import_order_debt`,
  `style_one_type_per_file_colocated_support_class`, `style_modifier_chain_padding_wrapping`,
  `style_extraction_refactor_multiarg_glomming`, `feedback_test_class_naming`, `style_conventions`
  (becomes the review-only section itself).
- Delete: `architecture_{settings,appearance,reading,onboarding}_*_redesign`,
  `architecture_library_tabs_all_entry{,_ui_followup}`, `project_m1a_active_session_controller`,
  `project_rhaydus_foundation_batch_{f9_f10_f8,i_build_gates}`, `project_bottombarscaffold_f2_f3_adoption`,
  `project_editorial_sheet_header_precedent_chain`, `project_component_library_migration_s1`,
  `project_detekt_gate_scope` (stale: `CLAUDE.md` now says test sources are gated),
  `project_architecture` (already in `CLAUDE.md`), `project_adaptive_ui_patterns` (check it against
  `layout.md` first), `project_rhaydus_foundation_upstream_migration` (already in
  `foundation-upstream-candidates.md`).

Destinations:
- The reviewer's kept memories → `.claude/agent-memory/softcover-reviewer/`, with a fresh `MEMORY.md`
  index.
- `unit-test-writer/` + `rhaydus-kotlin-unit-test-writer/` (29 files) → `softcover-test-writer/`,
  deduplicated with the same criteria; `feedback_import_order_convention` and
  `feedback_stale_project_import_order_in_core_book` are promoted or deleted, not kept.
- `rhaydus-kotlin-rhaydus-{logic,ui}/` (5 files) → `softcover-implementer/`, same criteria.
- Delete the emptied old directories. Leave `rhaydus-kotlin-rhaydus-adopt/` alone (adopt stays allowed).

## 6. Deny the originals (`.claude/settings.json`)

```json
"permissions": { "deny": [
  "Agent(rhaydus-kotlin:rhaydus-logic)", "Agent(rhaydus-kotlin:rhaydus-ui)",
  "Agent(rhaydus-kotlin:code-reviewer)", "Agent(rhaydus-kotlin:unit-test-writer)"
] }
```
Delete `.claude/agents/unit-test-writer.md`. `rhaydus-kotlin:rhaydus-adopt` stays allowed.

## 7. `.claude/hooks/agent-brief-check.sh` (PreToolUse, matcher `Agent`)

- Look at `tool_input.subagent_type` (field name confirmed by P2):
  - `softcover-implementer` needs `## Goal`, `## Files`, `## Verify`, `## Report`.
  - `softcover-test-writer` needs `## Files`, `## Verify`, `## Report`.
  - `softcover-reviewer` needs `## Scope`.
- On a miss, deny with a reason that includes the template for that agent (the templates live in
  `docs/reference/agent-briefs.md`, created in this step; the hook embeds the needed one so no extra read
  is required).
- Any other agent type passes through.

## 8. Personal auto-memory (this machine only)

Update `agents-allowed-for-project-workflow.md` under `~/.claude/projects/…/memory/` to name the
`softcover-*` agents. This is not part of the repo change.

## Acceptance

- Starting `softcover-implementer` with a bare prompt is denied with the template; with a complete brief
  it runs.
- `rhaydus-kotlin:code-reviewer` is refused by permissions.
- A smoke run of each agent on a trivial scoped task (e.g. the reviewer on the last commit) finishes in
  under 25 turns, with a report inside its limit.
- The memory directories match the triage, and the review-only section exists in
  `docs/reference/code-style.md`.
- `foundation-upstream.md` rows FU-1, FU-2 and FU-7 → "done locally".
