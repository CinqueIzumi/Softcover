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

- **State:** steps 1-3 done; the foundation reads 0.4.0 and ships every open `candidates.md` entry (user
  decisions). Step 4 wave 1 done: Konsist verified (0.18.1, go), components and style wiring moved upstream.
  `rememberShareCardCapture` takes a required `ioDispatcher` (user decision), which Softcover supplies through
  `LocalShareCardIoDispatcher` / `ProvideShareCardIoDispatcher`, mounted in `App.kt`.
- **Next:** step 4 waves, one per session, each wave's boxes as parallel agents. Now: wave 2, design tokens ∥
  build tooling. Then 3: ktlint ∥ detekt ∥ Konsist rules (foundation only; F62's halves go to the detekt and
  Konsist agents; the Konsist agent first swaps the five files still on Material `Text`: `MorphingButton`,
  `MastheadRefreshAction`, `RichText`, `SheetScaffold`, `ReviewsSection`). 4: sweeps in sequence:
  `ktlintFormat`, F29 inlining, F65 suppression audit. 5: docs and memory.
  Step 5's pointer box also moves Softcover's `README.md` foundation line and the managed `CLAUDE.md` block
  (re-run `rhaydus-adopt`) from 0.3.2 to 0.4.0.
- **Open questions:** none. The rules audit is folded into step 4 as F61-F67 (ktlint / detekt / Konsist split). The user's
  Medium_Phone pass (widget repaint on a time-zone change; Explore, Reading mark-as-read swipe, Settings
  rows) is pending but does not block step 4. Wave 1 adds the deadline date picker and the skeleton
  loading states to that pass.
- **Verification:** wave 1: foundation `check` and Softcover `check -x lint` green; reviewer findings fixed;
  moved tests pass. ktlint misses `),)` until F58 lands, so grep for it after each round.
- **Uncommitted:** wave 1 in both repos (the user approves the commits). Not pushed: foundation since
  `398f68c`, Softcover since `68dd22e4e`; push the foundation first.

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
| 4 | Clear the candidates backlog | [ ] |
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
- [ ] Design tokens: F38, F40, F43.
- [x] Verify Konsist on Softcover (F67's checks), before the rule boxes; a failure re-homes its rules to detekt.
- [x] Style wiring: F61 and F67's test module, before the rule boxes.
- [ ] ktlint formats only: F24, F25, F58, F63; F62 moves the semantic rules out.
- [ ] detekt rules: F29, F39, F41, F65, and the detekt halves of F57, F62, F64 and F66.
- [ ] Konsist assertions: F34, and the Konsist halves of F52, F57, F62, F64 and F66.
- [ ] Build tooling: F30, F50, F52.
- [ ] Docs and memory: F59; the review-only rules in both code-style docs shrink to what the gates leave;
      prune agent memories the new gates make stale.

### 5 — Ship

- [x] Bump the foundation version for everything since 0.3.1 (`gradle.properties` and `plugin.json`).
- [ ] Push the foundation branch, merge it, and tag the release (a `v*` tag publishes to Maven Central).
- [ ] Move Softcover's submodule pointer onto the tag; push `chore/foundation-submodule` and open its PR.
- [ ] Delete this README and `agent-layer.md` (emptied by step 2) and the `ACTIVE.md` line; `candidates.md` stays
      as the foundation backlog.

## Later (not this plan)

- The second app adopts the submodule, once the foundation is finished.
- Retire Maven Central publishing and the GitHub install instructions, after the second app builds from the
  submodule.

## Decisions

- Softcover consumes the foundation as a submodule at `foundation/` via `includeBuild`.
- Submodule over subtree: the foundation keeps its own history, PRs and tags.
- Softcover merges to `main` only with its submodule pointer on a tagged foundation commit.
- The foundation and every app that includes it build on the same AGP and Kotlin versions.
- One plugin, `rhaydus-kotlin`, loaded per project from its own `foundation/` checkout; no user-scope install.
- The plugin ships process; project facts live in the project; personal preferences stay with the user.
- Maven Central publishing stays live until the second app builds from the submodule.
- 0.4.0 ships the whole candidates backlog; nothing waits for a second app.
- ktlint formats, detekt judges calls, Konsist judges declarations; gates replace agent or grep checks lacking judgement.
- 2+-argument `Modifier` calls wrap one argument per line, like every other call.
- Project facts reach the plugin agents through an `## Agent parameters` table in the managed `CLAUDE.md` block.
- Plugin hooks register in the plugin `hooks.json`; the quiet-Gradle script ships in the plugin, not via adopt.
- The plugin agents drop the `softcover-` prefix; Softcover uses the plugin names after step 2.
- Commit style and engineering principles move to user scope, not into the plugin.
- Plugin hooks read project facts from `.claude/rhaydus.json`; a missing key means a default or the hook off.

## Baseline

| Metric | Before | After |
|---|---|---|
| Configuration time (`help --no-configuration-cache`) | 2.50 s | 3.09–3.19 s |
| Cold `assembleDebug --no-build-cache --rerun-tasks` | 41.9 s | 30.9–40.9 s |
