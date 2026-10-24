# Foundation — Implementation Plan

> **Lifecycle.** Delete this directory in the PR that finishes step 5. The roadmap lives in GitHub Issues (see
> `CLAUDE.md` § Roadmap); this is a working plan for one rollout.

**Goal:** Softcover builds `rhaydus-foundation` from the git submodule at `foundation/`, pinned on a tagged
release; the foundation owns the generic agent layer and the shared `build-logic` gates; both apps build it from
the submodule; Maven Central publishing and the GitHub plugin marketplace are retired.

**Rollout model:** foundation-side work lands as PRs on `CinqueIzumi/rhaydus-foundation`, edited through
Softcover's `foundation/` submodule; Softcover-side work is one branch, one PR per step; the second app adopts in
its own repo (step 3). Every step boundary leaves every build green. Sibling files:
[`agent-layer.md`](agent-layer.md) (the FU rows) and [`candidates.md`](candidates.md) (the F-numbered backlog).

**Issue:** not filed yet — file a `kind:tech` issue (no milestone) when step 1 starts.

## Now

- **State:** the submodule switch is built, verified (the PR gate and the iOS compile pass) and committed
  locally; nothing pushed.
- **Next:** step 0, when the user decides to share it.
- **Open questions:** none.
- **Uncommitted:** none.

## How to run a step

1. Start a fresh session (`/clear`) and ask it to resume from this `## Now` block.
2. Do the one step `## Now` names. Foundation-side items run in the `foundation/` submodule and follow the
   foundation's own `CLAUDE.md`.
3. Acceptance passes → tick the step below → update `## Now` → **ask the user before committing** (subject-only
   message, in each repo) → `/handoff` → the user types `/clear`.

## Steps

| Step | Scope | Repo | Status |
|---|---|---|---|
| 0 | Ship the submodule switch | both | [ ] |
| 1 | Foundation guardrails | foundation | [ ] |
| 2 | A generic agent layer upstream (FU-1 to FU-13), adopted in Softcover | both | [ ] |
| 3 | The second app adopts the submodule | second app | [ ] |
| 4 | Retire Maven Central publishing and the GitHub marketplace; upstream the `build-logic` gates | both | [ ] |
| 5 | Drain the foundation working docs and close out | both | [ ] |

### 0 — Ship the submodule switch

Softcover is on `chore/foundation-submodule`; the foundation is on its local `chore/softcover-submodule` branch
(version 0.3.2, unpushed).

- [ ] Push the foundation branch `chore/softcover-submodule`.
- [ ] Tag `v0.3.2` (a `v*` tag publishes to Maven Central).
- [ ] Move Softcover's submodule pointer onto the tag.
- [ ] Push `chore/foundation-submodule` and open its PR.

### 1 — Guardrails

No change originating in an app lands in the foundation before this step is done.

- [ ] Wire `detektCheck` into the foundation's `check`.
- [ ] Foundation CI runs `check` on every PR.
- [ ] Kotlin binary-compatibility-validator API dumps on every library module, committed; a public API change
      shows as a dump diff in review.
- [ ] A `CHANGELOG.md` with one entry per breaking change, and a semver tagging policy in the foundation README —
      the entries an app reads when it moves its pointer to a newer tag.
- [ ] `.claude/rules/foundation.md` in Softcover: update the API dump and changelog with every foundation change.

### 2 — Generic agent layer upstream

Port every row of [`agent-layer.md`](agent-layer.md) into `claude/plugins/rhaydus-kotlin/`, generic enough that
neither app needs a deny list; then adopt it in Softcover.

- [ ] Decide the generic shape: what the project supplies (agent names, brief templates, doc paths, Gradle
      wrapper) and how `rhaydus-adopt` writes it. Record it under `## Decisions`.
- [ ] FU-1, FU-7, FU-10: replace `rhaydus-logic` / `rhaydus-ui` / `code-reviewer` / `unit-test-writer` with the
      implementer / reviewer / test-writer trio.
- [ ] FU-2 to FU-5, FU-11, FU-12: the hooks, `/handoff` skill, quiet-Gradle script, token-usage script and
      doc-growth guards.
- [ ] FU-6: the catalogue-style managed `CLAUDE.md` block and the `.claude/rules/*.md` templates in
      `rhaydus-adopt`.
- [x] `rhaydus-adopt` wires the submodule, docs in place and the project-scoped plugin instead of Maven
      coordinates.
- [ ] FU-13: the docs-first reminder names the new agents.
- [ ] FU-8 and FU-9: split rule from rationale in `docs/`; the presentation file-size gate as a convention plugin.
- [ ] Tag a foundation release carrying all of it; move Softcover's submodule pointer to it.
- [ ] Softcover: re-run `rhaydus-adopt`; the `CLAUDE.md` local override becomes the managed block.
- [ ] Softcover: delete the four `Agent(rhaydus-kotlin:…)` deny rules, the duplicate `.claude/skills/style-check/`,
      and every local hook or script that now ships in the plugin.
- [ ] Softcover: `softcover-*` agents shrink to the Softcover-specific layer the generic shape allows.

### 3 — The second app adopts

Run in the second app's repo, by `rhaydus-adopt` from the step 2 tag.

- [ ] Submodule at `foundation/` on the same tag as Softcover, `includeBuild` wiring, CI submodule checkout.
- [ ] Plugin from the submodule at project scope; the managed `CLAUDE.md` block.
- [ ] Its local workarounds for foundation gaps filed as foundation issues.

### 4 — Retire publishing; upstream the gates

- [ ] Remove `com.vanniktech.maven.publish`, signing and the Central Portal configuration from the foundation;
      keep each module's `group` and artifact name (the included-build substitution coordinates).
- [ ] Remove the marketplace manifest's GitHub install instructions from the foundation README.
- [ ] F18, F20, F21 from [`candidates.md`](candidates.md): move `checkModuleGraph`, the `dependencyAnalysis`
      policy and the shared `lint.xml` into `foundation/build-logic`, consumed through
      `includeBuild("foundation/build-logic")`; delete the Softcover copies. Publishing them as Gradle plugins
      is not needed.

### 5 — Close out

- [ ] File every open entry of [`candidates.md`](candidates.md) as an issue on the foundation repo; delete the
      file.
- [ ] Delete [`agent-layer.md`](agent-layer.md) (emptied by step 2).
- [ ] `.claude/rules/docs.md`: the "Something the foundation should adopt" row points at foundation issues.
- [ ] Softcover's submodule pointer sits on a tag; delete this directory and its `ACTIVE.md` line.

## Decisions

- Softcover consumes the foundation as a submodule at `foundation/` via `includeBuild`.
- Submodule over subtree: the foundation keeps its own history, PRs and tags.
- Softcover merges to `main` only with its submodule pointer on a tagged foundation commit.
- Softcover switched before the agent layer goes upstream; the upstream work follows in steps 1 to 5.
- Maven Central publishing stays live until both apps build from the submodule (step 4 after step 3).
- The foundation and every app that includes it build on the same AGP and Kotlin versions.
- Upstream agents, hooks and skills are generic; app-specific names, briefs and doc paths stay in the app.
- Each app enables the plugin per project from its own `foundation/` checkout.

## Baseline

| Metric | Before | After |
|---|---|---|
| Configuration time (`help --no-configuration-cache`) | 2.50 s | 3.09–3.19 s |
| Cold `assembleDebug --no-build-cache --rerun-tasks` | 41.9 s | 30.9–40.9 s |
