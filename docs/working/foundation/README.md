# Foundation — Implementation Plan

> **Lifecycle.** Delete this README and `agent-layer.md` in the PR that finishes step 5; `candidates.md` stays. The roadmap lives in GitHub Issues (see
> `CLAUDE.md` § Roadmap); this is a working plan for one rollout.

**Goal:** the foundation is finished before any other app adopts it. Softcover builds it from the git submodule at
`foundation/`; the `rhaydus-kotlin` plugin carries a generic agent layer that any project can reuse, with
project facts read from the project; the bugs and gaps in code the foundation already ships are fixed; then the
whole thing is pushed and tagged in one go.

**Rollout model:** everything stays local until step 4. Foundation work is committed on the submodule's
`chore/softcover-submodule` branch, Softcover work on `chore/foundation-submodule`; nothing is pushed, tagged or
filed before step 4. Every step boundary leaves both builds green. Sibling files: [`agent-layer.md`](agent-layer.md)
(the FU rows) and [`candidates.md`](candidates.md) (the F-numbered backlog).

## Now

- **State:** steps 1-4 are done and committed, including FU-9 (foundation `rhaydus.file-size` `08da2fd`,
  Softcover `ed2b49f99`). Outside the plan, the verdict sheet now refuses swipe, outside tap and back while its
  draft is unsaved (Softcover `4067ab543`). Both branches are pushed and up to date with their upstreams
  (`chore/softcover-submodule`, `chore/foundation-submodule`); neither has a PR yet and `v0.4.0` is not
  tagged. The `## Later` items are out of scope.
- **Next:** step 5 in this README (`### 5`): open and merge the foundation PR for `chore/softcover-submodule`,
  tag `v0.4.0`; move Softcover's pointer onto the tag; re-run `rhaydus-adopt` so the managed `CLAUDE.md`
  block and the `README.md` foundation line say 0.4.0; reinstall the `rhaydus-kotlin` plugin so the reviewer
  prompt edit applies; push `chore/foundation-submodule` and open its PR; then delete this README,
  `agent-layer.md` and the `ACTIVE.md` line.
- **Open questions:** none. The user's Medium_Phone pass is pending and does not block step 5: widget repaint
  on a time-zone change, Explore / Reading swipe / Settings rows, the deadline date picker, skeletons,
  count-up and chart motion, nav icons, `RhaydusMorphingButton`, and the verdict sheet's unsaved-draft guard
  (installed on Medium_Phone).
- **Verification:** the full CI gate set passed on both repos before the verdict change (`check koverXmlReport
  :build-logic:test -x lint`, `lint buildHealth`, the iOS compiles, foundation `build`). After it,
  `styleCheck :konsist:test` and the host tests of `:core:uibinding`, `:core:component`,
  `:feature:book_detail` and `:feature:reading` pass; the reviewer's findings are fixed. The last session
  only re-dated commits and edited this doc, so the close-out gate (`styleCheck :konsist:test`) did not need
  re-running.
- **Uncommitted:** this tracker doc (handoff rewrite of `## Now`).

## How to run a step

1. Start a fresh session (`/clear`) and ask it to resume from this `## Now` block.
2. Do the one step `## Now` names. Foundation-side items run in the `foundation/` submodule and follow
   `.claude/rules/foundation.md`.
3. Acceptance passes → tick the step below → update `## Now` → **ask the user before committing** (subject-only
   message, in each repo) → `/handoff` → the user types `/clear`.

## Steps

| Step | Scope | Status |
|---|---|---|
| 1 | Audit Softcover's agent setup against the plugin | [x] |
| 2 | A generic agent layer in `rhaydus-kotlin`, adopted in Softcover | [x] |
| 3 | Fix what the foundation already ships | [x] |
| 4 | Clear the candidates backlog | [x] |
| 5 | Ship: push, tag, move the pointer | [ ] |

### 1 — Audit

Read-only. The output is a decision per item, recorded in `agent-layer.md` and under `## Decisions`.

- [x] Compare Softcover's `.claude/agents/softcover-*.md`, hooks, skills, rules and `docs/reference/agent-briefs.md`
      with the plugin's agents, hook and skill in `foundation/claude/plugins/rhaydus-kotlin/`.
- [x] Mine `.claude/agent-memory/**` for lessons the generic agents should carry.
- [x] Check every finding against the existing FU rows in `agent-layer.md` and the entries in `candidates.md`;
      one already covered updates that row or entry instead of adding a duplicate.
- [x] Per item: upstream (generic), stay in Softcover (project fact or personal preference), or drop.
- [x] Decide the generic shape: what the agents carry (process) and what each project supplies (doc paths, brief
      templates, Gradle wrapper, module names) and where `rhaydus-adopt` writes it.

### 2 — Generic agent layer

- [x] The implementer / reviewer / test-writer trio replaces `rhaydus-logic` / `rhaydus-ui` / `code-reviewer` /
      `unit-test-writer` (FU-1, FU-7, FU-10).
- [x] The hooks, `/handoff` skill, quiet-Gradle script, token-usage script and doc-growth guards the audit marked
      generic (FU-2 to FU-5, FU-11, FU-12, FU-14).
- [x] The plugin text names the new agents and no longer cites the sibling-checkout docs path (FU-13).
- [x] `rhaydus-adopt` writes the catalogue-style managed `CLAUDE.md` block and the `.claude/rules/*.md` templates
      (FU-6).
- [x] Split rule from rationale in `foundation/docs/` (FU-8).
- [x] Softcover adopts it: re-run `rhaydus-adopt`; delete the four deny rules, the duplicate
      `.claude/skills/style-check/` and every local hook, script or agent the plugin now ships; the `CLAUDE.md`
      local override becomes the managed block.

### 3 — Fix what the foundation already ships

Bugs and gaps in shared code only; new components, tokens and gates are step 4.

- [x] Component bugs: F26, F27, F31 + F46, F36, F45, F47, F48, F49, F51; delete Softcover's forks and the
      detekt bans on the foundation versions.
- [x] Contract gaps: F42, F44, F56.
- [x] Tooling: F33, F37, F54, F60; wire `detektCheck` into the foundation's `check`.
- [x] Data: F55.
- [x] Adopt F18, F20, F21 through `includeBuild("foundation/build-logic")`; delete the Softcover copies.
- [x] Lint the main sources of KMP library modules through `rhaydus.kmp.library`, gated by `check`.

### 4 — Clear the candidates backlog

Every open entry in `candidates.md` ships in 0.4.0. A box moves any Softcover-local copy into the foundation,
deletes it from Softcover, and prunes its entries. Boxes on disjoint modules run as parallel agents.

- [x] Components: F28, F32.
- [x] Components: F68.
- [x] Design tokens: F38, F40, F43.
- [x] Verify Konsist on Softcover (F67's checks), before the rule boxes; a failure re-homes its rules to detekt.
- [x] Style wiring: F61 and F67's test module, before the rule boxes.
- [x] ktlint formats only: F24, F25, F58, F63; F62 moves the semantic rules out.
- [x] detekt rules: F29, F39, F41, F65, and the detekt halves of F57, F62, F64 and F66.
- [x] Konsist assertions: F34, and the Konsist halves of F52, F57, F62, F64 and F66.
- [x] Build tooling: F30, F50, F52 (the Konsist half of F52 rides with the Konsist box).
- [x] Docs and memory: F59; the review-only rules in both code-style docs shrink to what the gates leave;
      prune agent memories the new gates make stale.

### 5 — Ship

- [x] Bump the foundation version for everything since 0.3.1 (`gradle.properties` and `plugin.json`).
- [ ] Remove Maven Central publishing, the `:catalog` artifact and the GitHub-install text from the foundation.
- [ ] Merge the foundation PR and tag the release.
- [ ] Move Softcover's submodule pointer onto the tag; push `chore/foundation-submodule` and open its PR.
- [ ] Delete this README and `agent-layer.md` (emptied by step 2) and the `ACTIVE.md` line; `candidates.md` stays
      as the foundation backlog.

## Later (not this plan)

- `BookDao` holds 123 functions against Softcover's `TooManyFunctions` limit of 100, behind `@Suppress`; the user
  reviews whether to split it by concern.
- The second app adopts the submodule, once the foundation is finished.

## Decisions

- Softcover consumes the foundation as a submodule at `foundation/` via `includeBuild`.
- Submodule over subtree: the foundation keeps its own history, PRs and tags.
- Softcover merges to `main` only with its submodule pointer on a tagged foundation commit.
- The foundation and every app that includes it build on the same AGP and Kotlin versions.
- One plugin, `rhaydus-kotlin`, loaded per project from its own `foundation/` checkout; no user-scope install.
- The plugin ships process; project facts live in the project; personal preferences stay with the user.
- The foundation is submodule-only: nothing is published, and a `v*` tag is a pin marker.
- 0.4.0 ships the whole candidates backlog; nothing waits for a second app.
- ktlint formats, detekt judges calls, Konsist judges declarations; gates replace agent or grep checks lacking judgement.
- 2+-argument `Modifier` calls wrap one argument per line, like every other call.
- Project facts reach the plugin agents through an `## Agent parameters` table in the managed `CLAUDE.md` block.
- Plugin hooks register in the plugin `hooks.json`; the quiet-Gradle script ships in the plugin, not via adopt.
- The plugin agents drop the `softcover-` prefix; Softcover uses the plugin names after step 2.
- Commit style and engineering principles move to user scope, not into the plugin.
- Plugin hooks read project facts from `.claude/rhaydus.json`; a missing key means a default or the hook off.
- Both builds run the Gradle daemon on JDK 17 (`gradle/gradle-daemon-jvm.properties`), like CI.
- Design-system modules are exempt from `RawSpacingDp` / `RawCornerDp`, but not from `FileLevelDpProperty`.
- Blank-line layout rules auto-fix in ktlint; only non-fixable judgements go to detekt.

## Baseline

| Metric | Before | After |
|---|---|---|
| Configuration time (`help --no-configuration-cache`) | 2.50 s | 3.09–3.19 s |
| Cold `assembleDebug --no-build-cache --rerun-tasks` | 41.9 s | 30.9–40.9 s |
