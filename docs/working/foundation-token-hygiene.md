# Token hygiene — changes to take up in the foundation

Softcover made these changes locally because the foundation (`nl.rhaydus` / the `rhaydus-kotlin` plugin)
couldn't be updated at the time. Each row says what changed, why, where the local version lives, and
where it belongs in the foundation. **Use this doc to pick the work up in a foundation session:** port
each row, release, re-run `rhaydus-adopt` in Softcover, then delete the local copy named in the row.

**Why, in one paragraph** (measured 2026-09-20 over 12 sessions / 109 subagent runs): 97% of tokens were
cache reads, i.e. re-sending context that was already there. Cost per session ≈ turns × context, and
sessions ran 150–600 turns at a mean context of 180K–420K. Plugin agents cost 13–15M tokens per run (about
100 turns) against 1.9M for the tightly briefed test-writer, and each started at ~48K tokens. The large
single-section docs and 2,000+-line screen files made every turn heavier. The baseline and the
re-measurement are in [Evidence](#evidence).

## Tracker

| ID | Change | Local location | Foundation target | Status |
|---|---|---|---|---|
| FU-1 | Agent redesign: one implementer per vertical slice (replaces logic + ui), tool allowlists, `maxTurns`, brief gate, reading discipline, ≤150-word reports; reviewer without "always read CLAUDE.md + style guide", review-only checklist, one review per stage; merged test-writer | `.claude/agents/softcover-{implementer,reviewer,test-writer}.md` | `claude/plugins/rhaydus-kotlin/agents/` — replace `rhaydus-logic`, `rhaydus-ui`, `code-reviewer`, `unit-test-writer` | done locally |
| FU-2 | Brief-check hook: refuses implementer / test-writer / reviewer spawns without the required brief sections | `.claude/hooks/agent-brief-check.sh`, `docs/reference/agent-briefs.md` | plugin `hooks/` + the brief templates in the plugin README | done locally |
| FU-3 | Session loop: `ACTIVE.md` + `## Now` convention, context-budget hook, `/handoff` skill | `.claude/hooks/context-budget.sh`, `.claude/skills/handoff/` | plugin hooks + skill; the convention in `docs/architecture.md` or a new `docs/working-docs.md` | done locally |
| FU-4 | Quiet Gradle wrapper + the rewrite hook | `scripts/gradle-quiet.sh`, `.claude/hooks/quiet-gradle.sh` | plugin hook + a script shipped by `rhaydus-adopt` | done locally |
| FU-5 | Large-read guard (main conversation, >600 lines without `limit`) | `.claude/hooks/large-read-guard.sh` | plugin hook | done locally |
| FU-6 | Catalogue-style `CLAUDE.md` managed block + path-scoped `.claude/rules/*.md` templates | the Rhaydus block in `CLAUDE.md` (local override), `.claude/rules/` | the `rhaydus-adopt` template (proposed text in [FU-6 proposed block](#fu-6-proposed-block)); adopt writes the rule files | done locally |
| FU-7 | Agent-memory hygiene: keep / promote / delete criteria; no stage snapshots; a rule applied every review becomes a checklist or lint item | the `softcover-*` agent instructions | the memory section of every plugin agent | done locally |
| FU-8 | Split rule from rationale in the vendored foundation docs (`code-style.md` 30KB, `architecture.md` 31KB, `design-system-foundations.md` 35KB) | not done locally (the docs are vendored and pinned) | `docs/` in the foundation repo | proposed |
| FU-9 | Presentation file-size gate (600 lines, no baseline) | root `build.gradle.kts` `checkPresentationFileSize` | a convention-plugin gate | done locally |
| FU-10 | Deny / retire the duplicate test-writer; drop "read CLAUDE.md every review" | `.claude/settings.json` deny rules | resolved by FU-1 | done locally |
| FU-11 | Token-usage analysis script | `scripts/claude/token-usage.py` | plugin `scripts/` | done locally |
| FU-12 | Doc-growth guards: doc-type contract + routing table, size budgets, write-time hook, `checkDocBudgets` gate, docs skill | `.claude/rules/docs.md`, `docs/doc-budgets.txt`, `.claude/hooks/doc-guard.sh`, `build-logic` `CheckDocBudgetsTask` (ratchet vs merge-base with `main`) | plugin hook + skill; the gate as a convention plugin; the contract in the foundation docs | done locally |
| FU-13 | Docs-first reminder hook still routes feature work to `rhaydus-logic` / `rhaydus-ui` (denied here since Step 03) and points at the managed Rhaydus block; it should name the implementer / reviewer / test-writer agents | not overridable locally (plugin hook); `CLAUDE.md` routing overrides it in practice | plugin `hooks/scripts/docs-first-reminder.sh`, updated with FU-1 | proposed |

Status values: `proposed` → `planned` → `done locally` → `upstreamed` (then delete the local copy).

## FU-6 proposed block

The exact text of the local override in `CLAUDE.md`, proposed as the `rhaydus-adopt` managed-block template. Upstream, the heading becomes `## Rhaydus foundation (managed by rhaydus-adopt - do not hand-edit)`, and the version, libraries and agent names are filled in from the project.

````markdown
## Rhaydus foundation (local override — pending foundation FU-6; re-running rhaydus-adopt reverts this)

- **Version:** nl.rhaydus foundation v0.3.1 from `mavenCentral()`. `foundation.local=true` in `local.properties`
  switches to `includeBuild("../rhaydus-foundation")`; the committed state is `false`.
- **Docs:** vendored and version-pinned at [`docs/rhaydus/0.3.1/`](docs/rhaydus/0.3.1) (architecture,
  toad-architecture, code-style, design-system-foundations, CAPABILITIES). They are the source of truth; this
  app's docs keep only its deltas.
- **Libraries consumed:**
  - State and logic: `toad`, `core-common`, `core-platform`, `offline-sync`.
  - Design system: `designsystem-core`, `designsystem-editorial`, `designsystem-image`.
  - Gates: `ktlint-rules`, `detekt-rules`.
- **Routing:** implementation → `softcover-implementer`; review → `softcover-reviewer`; tests →
  `softcover-test-writer`; style gates → the `style-check` skill. Foundation wiring changes (adding, removing
  or re-versioning an `nl.rhaydus` dependency) → the `rhaydus-adopt` agent.
- **Reuse-first:** check `CAPABILITIES.md` before hand-rolling a component, modifier or util.
````

## Evidence

Measured with `scripts/claude/token-usage.py` over the sessions started on or after 2026-09-25, the day the
tooling merged: 87 sessions (75 of at least 10 turns) and 333 subagent runs. **Before** is the 2026-09-20
baseline (12 sessions / 109 runs). Mean context is the mean context size per turn.

| Metric | Before | After | Target | Met |
|---|---|---|---|---|
| Main conversation: turns per working session | 156–604 | 11–96 (mean 46) | ≤150 | yes |
| Main conversation: mean context per turn | 182K–424K | 48K–132K (mean 90K) | ≤150K | yes |
| Main conversation: final context | 280K–710K | 54K–209K, no compactions | — | — |
| Implementer cost / turns per run (logic / ui before) | 14.6M / 14.7M, 105 / 101 | 5.6M, 56 (median 3.4M, 47) | ≤5M, ≤40 | yes since 2026-09-30, see below |
| Reviewer cost / turns per run | 13.5M, 99 | 1.6M, 23 | ≤5M, ≤40 | yes |
| Test-writer cost / turns per run | 1.9M | 1.4M, 23 | — | — |
| Subagent starting context (custom agents vs Explore) | ~48K vs ~11K | 18–19K vs 14K (general-purpose 31K) | ≤35K | yes |
| Presentation / `:core:component` files over 600 lines | 13 | 0, gated by `checkPresentationFileSize` | 0 | yes |

- **Implementer: oversized briefs drove the cost.** It is a long tail. The 12 costliest runs (14–40M,
  110–235 turns, ending at 160–315K context) each did a whole migration sub-step, building a component and
  moving every call site in one run. Runs started before 2026-09-30 13:49 (99) averaged 6.7M and 65 turns.
  Runs since then, one sub-step per session with smaller briefs (27), average 1.4M and 19 turns (max 6.9M).
  That meets the target, so no further change is needed.
- **`/write-docs` skill: not needed.** `doc-guard.sh` denied 53 writes in 22 transcripts (46 budget, 6
  history-line, 1 `## Now`), and reviewers raised no 🟡 doc-hygiene findings, so the hook plus
  `.claude/rules/docs.md` are enough. About half the budget denials hit `components.md`, whose 8 KB budget
  left no room for new index lines; it is re-pinned at 10 KB until the migration's final doc pass.
- **Transcript location.** `token-usage.py` reads transcripts from `$CLAUDE_CONFIG_DIR/projects`, falling
  back to `~/.claude/projects`; the upstream port (FU-11) keeps that.
