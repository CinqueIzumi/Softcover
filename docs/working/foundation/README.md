# Foundation — Implementation Plan

> **Lifecycle.** Delete this README and `agent-layer.md` in the PR that finishes step 4; `candidates.md` stays. The roadmap lives in GitHub Issues (see
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

- **State:** the build-logic adoption is done. `rhaydus.module-graph` gained `allowedProjects`,
  `bannedEdges`, `bannedExternalGroups`, `ignoredExternalCoordinates` and snapshots edges at configuration
  time; the new root-applied `rhaydus.dependency-analysis` carries the shared buildHealth policy;
  `rhaydus.kmp.compose` uses `navigationevent-compose`. Softcover applies both gates, its library modules
  apply `rhaydus.kmp.library`, and `softcover.kmp.compose` is `rhaydus.kmp.compose` + the stability gate.
  Injected violations proved module-graph, DA and stability still fail; lint did not (see Next).
- **Next:** step 3, last box: library modules (`com.android.kotlin.multiplatform.library`) get no
  main-source lint task, so a warning in `core/book/src/androidMain` passes everything, `:app:lintDebug`
  with `checkDependencies = true` included. Find how that plugin enables main-source lint, wire it in
  `rhaydus.kmp.library` and `check`, prove it with an injected `SdCardPath`, fix what it surfaces.
- **Open questions:** none.
- **Verification:** Softcover and foundation `check buildHealth` green, foundation build-logic tests green.
- **Uncommitted:** nothing. Not pushed: foundation since `398f68c`, and Softcover's commits since
  `68dd22e4e`; push the foundation first.

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
| 2 | A generic agent layer in `rhaydus-kotlin`, adopted in Softcover | [ ] |
| 3 | Fix what the foundation already ships | [ ] |
| 4 | Ship: push, tag, move the pointer | [ ] |

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

Bugs and gaps in shared code only; new components, design tokens and house-style lint rules wait for a second app.

- [x] Component bugs: F26, F27, F31 + F46, F36, F45, F47, F48, F49, F51; delete Softcover's forks and the
      detekt bans on the foundation versions.
- [x] Contract gaps: F42, F44, F56.
- [x] Tooling: F33, F37, F54, F60; wire `detektCheck` into the foundation's `check`.
- [x] Data: F55.
- [x] Adopt F18, F20, F21 through `includeBuild("foundation/build-logic")`; delete the Softcover copies.
- [ ] Lint the main sources of KMP library modules through `rhaydus.kmp.library`, gated by `check`.

### 4 — Ship

- [ ] Bump the foundation version for everything since 0.3.1 (`gradle.properties` and `plugin.json`).
- [ ] Push the foundation branch, merge it, and tag the release (a `v*` tag publishes to Maven Central).
- [ ] Move Softcover's submodule pointer onto the tag; push `chore/foundation-submodule` and open its PR.
- [ ] Delete this README and `agent-layer.md` (emptied by step 2) and the `ACTIVE.md` line; `candidates.md` stays
      as the foundation backlog.

## Later (not this plan)

- The second app adopts the submodule, once the foundation is finished.
- Retire Maven Central publishing and the GitHub install instructions, after the second app builds from the
  submodule.
- Softcover-specific components, design tokens and house-style lint rules in `candidates.md`, extracted when a
  second app wants them.

## Decisions

- Softcover consumes the foundation as a submodule at `foundation/` via `includeBuild`.
- Submodule over subtree: the foundation keeps its own history, PRs and tags.
- Softcover merges to `main` only with its submodule pointer on a tagged foundation commit.
- The foundation and every app that includes it build on the same AGP and Kotlin versions.
- One plugin, `rhaydus-kotlin`, loaded per project from its own `foundation/` checkout; no user-scope install.
- The plugin ships process; project facts live in the project; personal preferences stay with the user.
- Maven Central publishing stays live until the second app builds from the submodule.
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
