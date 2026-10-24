# Token hygiene — changes to take up in the foundation

Local changes to port into `foundation/claude/plugins/rhaydus-kotlin/`, then delete the local copy. Each row says
what changed, where the local version lives, and where it belongs in the foundation.

## Tracker

| ID | Change | Local location | Foundation target | Status |
|---|---|---|---|---|
| FU-1 | Agent redesign: one implementer per vertical slice (replaces logic + ui), tool allowlists, `maxTurns`, brief gate, reading discipline, ≤150-word reports; reviewer without "always read CLAUDE.md + style guide", review-only checklist, one review per stage; merged test-writer | `.claude/agents/softcover-{implementer,reviewer,test-writer}.md` | `claude/plugins/rhaydus-kotlin/agents/` — replace `rhaydus-logic`, `rhaydus-ui`, `code-reviewer`, `unit-test-writer` | done locally |
| FU-2 | Brief-check hook: refuses implementer / test-writer / reviewer spawns without the required brief sections | `.claude/hooks/agent-brief-check.sh`, `docs/reference/agent-briefs.md` | plugin `hooks/` + the brief templates in the plugin README | done locally |
| FU-3 | Session loop: `ACTIVE.md` + `## Now` convention, context-budget hook, `/handoff` skill | `.claude/hooks/context-budget.sh`, `.claude/skills/handoff/` | plugin hooks + skill; the convention in `docs/architecture.md` or a new `docs/working-docs.md` | done locally |
| FU-4 | Quiet Gradle wrapper + the rewrite hook | `scripts/gradle-quiet.sh`, `.claude/hooks/quiet-gradle.sh` | plugin hook + a script shipped by `rhaydus-adopt` | done locally |
| FU-5 | Large-read guard (main conversation, >600 lines without `limit`) | `.claude/hooks/large-read-guard.sh` | plugin hook | done locally |
| FU-6 | Catalogue-style `CLAUDE.md` managed block + path-scoped `.claude/rules/*.md` templates | the Rhaydus block in `CLAUDE.md` (local override), `.claude/rules/` | the `rhaydus-adopt` template; adopt writes the rule files | partly upstreamed: `rhaydus-adopt` wires the submodule and the plugin; the catalogue-style block and the rules templates are still local |
| FU-7 | Agent-memory hygiene: keep / promote / delete criteria; no stage snapshots; a rule applied every review becomes a checklist or lint item | the `softcover-*` agent instructions | the memory section of every plugin agent | done locally |
| FU-8 | Split rule from rationale in the foundation docs (`code-style.md` 30KB, `architecture.md` 31KB, `design-system-foundations.md` 35KB) | not done locally | `docs/` in the foundation repo | proposed |
| FU-9 | Presentation file-size gate (600 lines, no baseline) | root `build.gradle.kts` `checkPresentationFileSize` | a convention-plugin gate | done locally |
| FU-10 | Deny / retire the duplicate test-writer; drop "read CLAUDE.md every review" | `.claude/settings.json` deny rules | resolved by FU-1 | done locally |
| FU-11 | Token-usage analysis script | `scripts/claude/token-usage.py` | plugin `scripts/` | done locally |
| FU-12 | Doc-growth guards: doc-type contract + routing table, size budgets, write-time hook, `checkDocBudgets` gate, docs skill | `.claude/rules/docs.md`, `docs/doc-budgets.txt`, `.claude/hooks/doc-guard.sh`, `build-logic` `CheckDocBudgetsTask` (ratchet vs merge-base with `main`) | plugin hook + skill; the gate as a convention plugin; the contract in the foundation docs | done locally |
| FU-13 | Docs-first reminder hook still routes feature work to `rhaydus-logic` / `rhaydus-ui` (denied here) and points at the managed Rhaydus block; it should name the implementer / reviewer / test-writer agents | not overridable locally (plugin hook); `CLAUDE.md` routing overrides it in practice | plugin `hooks/scripts/docs-first-reminder.sh`, updated with FU-1 | proposed |

Status values: `proposed` → `planned` → `done locally` → `upstreamed` (then delete the local copy).
