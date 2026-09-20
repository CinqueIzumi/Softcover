# Token hygiene — changes to take up in the foundation

Softcover made these changes locally because the foundation (`nl.rhaydus` / the `rhaydus-kotlin` plugin)
couldn't be updated at the time. Each row says what changed, why, where the local version lives, and
where it belongs in the foundation. **Use this doc to pick the work up in a foundation session:** port
each row, release, re-run `rhaydus-adopt` in Softcover, then delete the local copy named in the row.

**Why, in one paragraph** (measured 2026-09-20 over 12 sessions / 109 subagent runs): 97% of tokens were
cache reads, i.e. re-sending context that was already there. Cost per session ≈ turns × context, and
sessions ran 150–600 turns at a mean context of 180K–420K. Plugin agents cost 13–15M tokens per run (about
100 turns) against 1.9M for the tightly briefed test-writer, and each started at ~48K tokens. The large
single-section docs and 2,000+-line screen files made every turn heavier. The full baseline is in
`docs/working/token-hygiene/README.md`.

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
| FU-9 | Presentation file-size gate (600 lines, no baseline) | root `build.gradle.kts` `checkPresentationFileSize` | a convention-plugin gate | planned |
| FU-10 | Deny / retire the duplicate test-writer; drop "read CLAUDE.md every review" | `.claude/settings.json` deny rules | resolved by FU-1 | done locally |
| FU-11 | Token-usage analysis script | `scripts/claude/token-usage.py` | plugin `scripts/` | done locally |
| FU-12 | Doc-growth guards: doc-type contract + routing table, size budgets, write-time hook, `checkDocBudgets` gate, docs skill | see Step 04b | plugin hook + skill; the gate as a convention plugin; the contract in the foundation docs | proposed |
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

Filled in by Step 09: the before/after table.
