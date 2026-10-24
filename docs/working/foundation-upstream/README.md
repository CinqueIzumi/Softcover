# Foundation Upstream — Implementation Plan

> **Lifecycle.** Delete this directory in the PR that finishes step 5. The roadmap lives in GitHub Issues (see
> `CLAUDE.md` § Roadmap); this is a working plan for one rollout.

**Goal:** the foundation owns the generic agent layer and the shared `build-logic` gates, both apps build it from
a submodule, and Maven Central publishing and the GitHub plugin marketplace are retired. Builds on
[`../foundation-submodule/README.md`](../foundation-submodule/README.md), which puts Softcover on the submodule.

**Rollout model:** foundation-side work lands as PRs on `CinqueIzumi/rhaydus-foundation`, edited through
Softcover's `foundation/` submodule; Softcover-side work is one branch, one PR per step; the second app adopts in
its own repo (step 3). Every step boundary leaves every build green.

**Issue:** not filed yet — file a `kind:tech` issue (no milestone) when step 1 starts.

## Now

- **State:** not started; deferred.
- **Due when:** `../foundation-submodule/` is deleted, and `docs/working/architecture-review.md` and
  `docs/working/compose-audit-report.md` are both deleted. When that holds, point `docs/working/ACTIVE.md` at
  this file.
- **Next:** step 1 (foundation guardrails).
- **Open questions:** none.
- **Verification:** not run.
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
| 1 | Foundation guardrails | foundation | [ ] |
| 2 | A generic agent layer upstream (FU-1 to FU-13), adopted in Softcover | both | [ ] |
| 3 | The second app adopts the submodule | second app | [ ] |
| 4 | Retire Maven Central publishing and the GitHub marketplace; upstream the `build-logic` gates | both | [ ] |
| 5 | Drain the foundation working docs and close out | both | [ ] |

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

Port every row of [`../foundation-token-hygiene.md`](../foundation-token-hygiene.md) into
`claude/plugins/rhaydus-kotlin/`, generic enough that neither app needs a deny list; then adopt it in Softcover.

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
- [ ] F18, F20, F21 from [`../foundation-upstream-candidates.md`](../foundation-upstream-candidates.md): move
      `checkModuleGraph`, the `dependencyAnalysis` policy and the shared `lint.xml` into the foundation's
      `build-logic`; delete the Softcover copies.

### 5 — Close out

- [ ] File every open entry of `../foundation-upstream-candidates.md` as an issue on the foundation repo; delete
      the file.
- [ ] Delete `../foundation-token-hygiene.md` (emptied by step 2).
- [ ] `.claude/rules/docs.md`: the "Something the foundation should adopt" row points at foundation issues.
- [ ] Softcover's submodule pointer sits on a tag; delete this directory and its `ACTIVE.md` line.

## Decisions

- Upstream agents, hooks and skills are generic; app-specific names, briefs and doc paths stay in the app.
- Each app enables the plugin per project from its own `foundation/` checkout.
- An app merges to `main` only with its submodule pointer on a tagged foundation commit.
- Publishing is retired only after both apps build from the submodule (step 4 after step 3).
