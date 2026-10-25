# Token hygiene — changes to take up in the foundation

Local changes to port into `foundation/claude/plugins/rhaydus-kotlin/`, then delete the local copy. Each row says
what changed, where the local version lives, and where it belongs in the foundation.

## Tracker

| ID | Change | Local location | Foundation target | Status |
|---|---|---|---|---|
| FU-1 | Agent redesign: one implementer per vertical slice (replaces logic + ui), tool allowlists, `maxTurns`, brief gate, reading discipline, ≤150-word reports; reviewer without "always read CLAUDE.md + style guide", review-only checklist, one review per stage; merged test-writer. Upstream without the `softcover-` prefix; doc tables, task names and module roles come from the project parameters (§ Generic shape); carry the plugin lines in § Keep; fix the reviewer's duplicate item 8 (`softcover-reviewer.md:60,67`) | `.claude/agents/softcover-{implementer,reviewer,test-writer}.md` | `claude/plugins/rhaydus-kotlin/agents/` — replace `rhaydus-logic`, `rhaydus-ui`, `code-reviewer`, `unit-test-writer` | upstreamed |
| FU-2 | Brief-check hook: refuses implementer / test-writer / reviewer spawns without the required brief sections. The section check and its case tests are generic; agent names, template Verify lines and the brief-doc path are parameters. The brief skeleton upstreams; the example commands stay in `docs/reference/agent-briefs.md` | `.claude/hooks/agent-brief-check.sh`, `.claude/hooks/test/run-agent-brief-cases.sh`, `docs/reference/agent-briefs.md` | plugin `hooks/` + the brief skeleton in the plugin README | upstreamed |
| FU-3 | Session loop: `ACTIVE.md` + `## Now` convention, context-budget hook, `/handoff` skill. Rename `SOFTCOVER_CONTEXT_BUDGET`; the close-out gate list and the commit style are project / user inputs, not skill text | `.claude/hooks/context-budget.sh`, `.claude/skills/handoff/` | plugin hooks + skill; the convention in `docs/architecture.md` or a new `docs/working-docs.md` | upstreamed |
| FU-4 | Quiet Gradle wrapper + the rewrite hook + its case tests. Ship the script in the plugin `scripts/` (not copied by adopt). **Bug:** both pin the root to `CLAUDE_PROJECT_DIR` or the script's parent (`quiet-gradle.sh:5`, `gradle-quiet.sh:4`), so runs from a worktree or `foundation/` hit the main checkout; resolve the nearest `gradlew` from the cwd | `scripts/gradle-quiet.sh`, `.claude/hooks/quiet-gradle.sh` | plugin hook + plugin `scripts/` | upstreamed |
| FU-5 | Large-read guard (main conversation, >600 lines without `limit`); its message names `softcover-implementer` (`large-read-guard.sh:27`) | `.claude/hooks/large-read-guard.sh` | plugin hook | upstreamed |
| FU-6 | Catalogue-style `CLAUDE.md` managed block + path-scoped `.claude/rules/*.md` templates. The block carries the routing threshold, "tests always delegated", reviewer-after-build, the failure relay and the `## Agent parameters` table; adopt writes skeletons of `architecture`, `compose-ui`, `kotlin-style`, `build-wiring`, `tests`, `docs` and `foundation` rules holding the foundation lines, the project fills the rest | the Rhaydus block in `CLAUDE.md` (local override), `.claude/rules/`, `CLAUDE.md` § Working in this repo and § Tests | the `rhaydus-adopt` template; adopt writes the rule files | partly upstreamed: `rhaydus-adopt` wires the submodule and the plugin; the catalogue-style block and the rules templates are still local |
| FU-7 | Agent-memory hygiene: keep / promote / delete criteria; no stage snapshots; a rule applied every review becomes a checklist or lint item. The lessons in § Agent memory marked generic become agent instructions | the `softcover-*` agent instructions, `.claude/agent-memory/**` | the memory section and body of every plugin agent | upstreamed |
| FU-8 | Split rule from rationale in the foundation docs (`code-style.md` 30KB, `architecture.md` 31KB, `design-system-foundations.md` 35KB) | not done locally | `docs/` in the foundation repo | proposed |
| FU-9 | Presentation file-size gate (600 lines, no baseline) | root `build.gradle.kts` `checkPresentationFileSize` | a convention-plugin gate | done locally |
| FU-10 | Deny / retire the duplicate test-writer; drop "read CLAUDE.md every review". The four deny rules (`settings.json:4-9`) go once step 2 lands | `.claude/settings.json` deny rules | resolved by FU-1 | planned |
| FU-11 | Token-usage analysis script; no project facts in it | `scripts/claude/token-usage.py` | plugin `scripts/` | upstreamed |
| FU-12 | Doc-growth guards: doc-type contract + routing table, size budgets, write-time hook, `checkDocBudgets` gate, docs skill. The mechanism upstreams; budgets, doc paths, routing hints, the permanent-doc list and plan-ID patterns move into a project config (`doc-guard.sh:9-10,99-104,157,175-181,196,199`). The hook and the Gradle task read one predicate source | `.claude/rules/docs.md`, `docs/doc-budgets.txt`, `.claude/hooks/doc-guard.sh`, `doc-guard.cases`, `build-logic` `CheckDocBudgetsTask` (ratchet vs merge-base with `main`) | plugin hook + skill; the gate as a convention plugin; the contract in the foundation docs | upstreamed |
| FU-13 | The plugin still describes the old agents: the docs-first reminder routes to `rhaydus-logic` / `rhaydus-ui` (`docs-first-reminder.sh:29`), `plugin.json:4` describes them, `rhaydus-logic.md` / `rhaydus-ui.md` cite `../rhaydus-foundation/docs/` and `foundation.local=true`, and `rhaydus-logic.md:23` names `:core:database` | not overridable locally (plugin files); `CLAUDE.md` routing overrides it in practice | plugin `hooks/scripts/docs-first-reminder.sh`, `plugin.json`, updated with FU-1 | upstreamed |
| FU-14 | Slow-Gradle blocker: refuses long-running Gradle tasks in the main conversation. Any KMP + iOS consumer has the same problem; the blocked task list is a parameter | `.claude/hooks/block-slow-gradle.sh` | plugin hook | upstreamed |

Status values: `proposed` → `planned` → `done locally` → `upstreamed` (then delete the local copy).

## Generic shape

The plugin agents carry process: the brief gate, reading discipline, tool-call budget, report shapes, the review
checklist (correctness, architecture, on-touch sweep, doc hygiene, comments, test checks, stability-baseline
diff), test triage and AAA / `@Nested` structure, the memory criteria and the session loop.

Each project supplies an `## Agent parameters` table inside the managed `CLAUDE.md` block (agents already have
it in context):

| Parameter | Softcover value |
|---|---|
| Compile verify task | `compileKotlinJvm` |
| Unit test task / source set | `testAndroidHostTest` / `src/androidHostTest` |
| Full gate / auto-fix | `check buildHealth` / `ktlintFormat lintFix` |
| Close-out gates (`/handoff`) | `styleCheck`, the review-only test checks |
| Test package prefix | `nl.rhaydus.softcover` |
| Doc paths | architecture delta, design-system index, voice, review-only rules, `r8.md`, `tests.md` |
| Foundation docs | `foundation/docs/` |
| Brief doc | `docs/reference/agent-briefs.md` |
| Module roles | database home `:core:database`, component home `:core:component` |
| Stability task | `stabilityDump` |
| Working docs / pointer | `docs/working/`, `docs/working/ACTIVE.md` |
| Doc budgets / routing | `docs/doc-budgets.txt` + the doc-guard config |
| Context budget | the context-budget hook's threshold |

`rhaydus-adopt` writes the table, the rule skeletons (FU-6), `docs/reference/agent-briefs.md` with project
examples, and `docs/doc-budgets.txt`. Hooks register through the plugin `hooks.json`, not `settings.json`;
the Gradle wrapper is found from the cwd.

## Keep from the plugin agents

- `rhaydus-ui`: the app doc wins for brand values, the foundation for structure; no design doc yet → neutral
  defaults and surface the brand choices; never copy foundation component source; the reuse map
  (`RhaydusButton`, `AdaptiveModalSheet`, `playDecorativeMotion`, editorial, image).
- `rhaydus-logic`: wire a missing foundation module onto the classpath; `## Contract` as the UI-spec handoff.
- `code-reviewer`: the security check; when reviewing the foundation itself, a `CAPABILITIES.md` / doc divergence
  is blocking; understand intent first; verify a memory before acting on it.
- `unit-test-writer`: the edge-case checklist (numbers, dates and DST, unicode); discover the framework when no
  conventions doc exists; tests must fail if the code broke.
- `rhaydus-adopt`: all of it, plus "a wiring pass never migrates code" (from its agent memory) and FU-6.

## Agent memory

| Lessons | Decision |
|---|---|
| Implementer: check the working tree first; `styleCheck` pulls unrelated module failures; verify a "clean" claim by running the gate; Kotlin block comments nest | generic instruction (implementer, test-writer) |
| File splits are pure moves (implementer `file_split_import_verification`, reviewer `file_split_body_and_import_drift`, `file_split_kdoc_link_scope`) | one merged generic instruction |
| Migrations diff every property of every branch (implementer `component_migration_style_drops`, reviewer `scaffold_merge_widens_fillmaxwidth`) | generic instruction |
| Reviewer: design-system doc drift, staged vs unstaged scope, hook script not registered, new component shadows a foundation one, UiState field never populated, redundant default-param override, `onStart` StateFlow double emit, heading semantics need a text leaf | generic reviewer checklist |
| Test-writer: host test task name (`jvmTest` reports NO-SOURCE as green), MockK / Kotest gotchas, coroutine-test scheduling, flow emission assertions, paged-flow early cancellation, virtual time source, MockK static hoisting, change-detector prune judgment, hook and Gradle-task testing | generic instruction |
| Implementer `matching_declaration_name…` | foundation `kotlin-style` rule template |
| Implementer `import_order_not_autofixed` | lint, covered by F24 |
| Reviewer `r10_computed_getter_trap` | lint, F57 |
| Test-writer `ktlintformat_nested_call_trailing_comma` | formatter bug, F58 |
| Reviewer `full_height_sheet_pinning`, `foundation_self_emitting_modifiers`, `embedded_content_screenmodel_scoping` | foundation docs, F59; then delete from memory |
| Reviewer `dual_gate_predicate_parity` | delete once FU-12 shares one predicate source |
| Implementer `detekt_builtin_kotlin_silent_loss`, `app_module_lacks_toad_voyager_koin` | Softcover `build-wiring.md` rule (related: F33) |
| Reviewer `new_shared_component_missing_catalogue_line` | Softcover `compose-ui.md` rule |
| Reviewer `gradle_main_suffix_case_sensitive`, `arrange_filter…`, `chipset…`, `r9_citation…`; test-writer project conventions, canonical templates, `sessionvaluecache_testing`, the Apollo notes, `migration_test_pattern`, `app_module_testdebug…`, `composable_style_resolution…`, `room_upsert_conflict_*` (merge the two duplicates) | stay in Softcover memory |

## Stays in Softcover

- Skills `release`, `set-version-name`, `increment-version-code`, `update-readme`: version files and README layout
  differ per app. The local `style-check` copy is dropped for the plugin's (FU-6), after porting its "read the
  summary, grep the log" line; its full-gate command becomes a parameter.
- `.claude/rules/roadmap.md` and the Softcover lines of the other rules (modules, Apollo, Room, `SpinePalette`).
- `CLAUDE.md` § Engineering principles and § Commit messages are personal preferences: they move to user scope,
  not into the plugin.
