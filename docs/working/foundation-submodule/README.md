# Foundation Submodule — Implementation Plan

> **Lifecycle.** Delete this directory in the PR that finishes step 7. The roadmap lives in GitHub Issues (see
> `CLAUDE.md` § Roadmap); this is a working plan for one rollout.

**Goal:** every consuming app — Softcover and the second app — builds `rhaydus-foundation` from a git submodule,
so one session can change the foundation (libraries, `build-logic`, docs and the `rhaydus-kotlin` plugin) and the
app together. Each app pins its own foundation commit; tags mark the commits apps move between. Maven Central
publishing and the GitHub plugin marketplace are retired.

**Rollout model:** foundation-side work lands as PRs on `CinqueIzumi/rhaydus-foundation`; Softcover-side work is
one branch, one PR; the second app adopts in its own repo (step 5). Every step boundary leaves every build green.

**Issue:** not filed yet — file a `kind:tech` issue (no milestone) when step 1 starts.

## Now

- **State:** not started; queued behind the current working docs.
- **Due when:** step 1 in this file — `docs/working/architecture-review.md` and
  `docs/working/compose-audit-report.md` are both deleted. When that holds, point `docs/working/ACTIVE.md` at
  this file.
- **Next:** step 1 (foundation guardrails).
- **Open questions:** none.
- **Verification:** not run.
- **Uncommitted:** none.

## How to run a step

1. Start a fresh session (`/clear`) and ask it to resume from this `## Now` block.
2. Do the one step `## Now` names. Foundation-side items run in the `foundation/` checkout (or a standalone
   clone before step 3) and follow the foundation's own `CLAUDE.md`.
3. Acceptance passes → tick the step below → update `## Now` → **ask the user before committing** (subject-only
   message, in each repo) → `/handoff` → the user types `/clear`.

## Steps

| Step | Scope | Repo | Status |
|---|---|---|---|
| 1 | Foundation guardrails | foundation | [ ] |
| 2 | A generic agent layer upstream (FU-1 to FU-13) | foundation | [ ] |
| 3 | Softcover: the submodule and the Gradle wiring | Softcover | [ ] |
| 4 | Softcover: the plugin from the submodule, docs from the submodule, overrides deleted | Softcover | [ ] |
| 5 | The second app adopts the submodule | second app | [ ] |
| 6 | Retire Maven Central publishing and the GitHub marketplace; upstream the `build-logic` gates | both | [ ] |
| 7 | Drain the foundation working docs and close out | both | [ ] |

### 1 — Guardrails

No change originating in an app lands in the foundation before this step is done.

- [ ] Wire `detektCheck` into the foundation's `check`.
- [ ] Foundation CI runs `check` on every PR.
- [ ] Kotlin binary-compatibility-validator API dumps on every library module, committed; a public API change
      shows as a dump diff in review.
- [ ] A `CHANGELOG.md` with one entry per breaking change, and a semver tagging policy in the foundation README —
      the entries an app reads when it moves its pointer to a newer tag.

### 2 — Generic agent layer upstream

Port every row of [`../foundation-token-hygiene.md`](../foundation-token-hygiene.md) into
`claude/plugins/rhaydus-kotlin/`, generic enough that neither app needs a deny list.

- [ ] Decide the generic shape: what the project supplies (agent names, brief templates, doc paths, Gradle
      wrapper) and how `rhaydus-adopt` writes it. Record it under `## Decisions`.
- [ ] FU-1, FU-7, FU-10: replace `rhaydus-logic` / `rhaydus-ui` / `code-reviewer` / `unit-test-writer` with the
      implementer / reviewer / test-writer trio.
- [ ] FU-2 to FU-5, FU-11, FU-12: the hooks, `/handoff` skill, quiet-Gradle script, token-usage script and
      doc-growth guards.
- [ ] FU-6: the catalogue-style managed `CLAUDE.md` block and the `.claude/rules/*.md` templates in
      `rhaydus-adopt`; `rhaydus-adopt` wires the submodule instead of Maven coordinates.
- [ ] FU-13: the docs-first reminder names the new agents.
- [ ] FU-8 and FU-9: split rule from rationale in `docs/`; the presentation file-size gate as a convention plugin.
- [ ] Tag a foundation release carrying all of it.

### 3 — Softcover: submodule and Gradle wiring

- [ ] `git submodule add` the foundation at `foundation/`, on the step 2 tag.
- [ ] `settings.gradle.kts`: `includeBuild("foundation")` unconditionally, and `foundation/build-logic` in
      `pluginManagement`; delete the `foundation.local` switch and its `local.properties` key.
- [ ] `gradle/libs.versions.toml`: drop `rhaydusFoundation`; the `rhaydus-*` entries stay version-less, as
      substitution coordinates for the included build.
- [ ] CI (`ci.yml`, `release.yml`) checks out submodules; the iOS / Xcode build resolves them too.
- [ ] `README.md` clone instructions use `--recurse-submodules`.
- [ ] `.claude/rules/foundation.md`, path-scoped to `foundation/**`: work on a branch inside the submodule, never
      a detached HEAD; push the foundation commit before moving the pointer; generic-only changes; update the
      API dump and changelog; ask before committing.
- [ ] `.claude/rules/build-wiring.md` gets the submodule build gotchas found while doing this.
- [ ] Measure configuration time and a cold `assembleDebug` before and after; record both under `## Baseline`.

### 4 — Softcover: plugin, docs, overrides

- [ ] Project-scoped marketplace pointing at `foundation/` in `.claude/settings.json`; drop the user-scope
      GitHub install.
- [ ] Confirm how plugin edits take effect (live, `/plugin update`, or a restart) and write it into
      `.claude/rules/foundation.md`.
- [ ] Re-run `rhaydus-adopt`; the `CLAUDE.md` local override becomes the managed block.
- [ ] Delete the four `Agent(rhaydus-kotlin:…)` deny rules, the duplicate `.claude/skills/style-check/`, and
      every local hook or script that now ships in the plugin.
- [ ] `softcover-*` agents shrink to the Softcover-specific layer the step 2 shape allows.
- [ ] Delete `docs/rhaydus/0.3.1/`; repoint `CLAUDE.md`, `docs/reference/**` and `.claude/rules/docs.md` at
      `foundation/docs/`; drop the vendored-docs line from `.claude/rules/docs.md`.

### 5 — The second app adopts

Run in the second app's repo, by `rhaydus-adopt` from the step 2 tag.

- [ ] Submodule at `foundation/` on the same tag as Softcover, `includeBuild` wiring, CI submodule checkout.
- [ ] Plugin from the submodule at project scope; the managed `CLAUDE.md` block.
- [ ] Its local workarounds for foundation gaps filed as foundation issues.

### 6 — Retire publishing; upstream the gates

- [ ] Remove `com.vanniktech.maven.publish`, signing and the Central Portal configuration from the foundation;
      keep each module's `group` and artifact name (the included-build substitution coordinates).
- [ ] Remove the marketplace manifest's GitHub install instructions from the foundation README.
- [ ] F18, F20, F21 from [`../foundation-upstream-candidates.md`](../foundation-upstream-candidates.md): move
      `checkModuleGraph`, the `dependencyAnalysis` policy and the shared `lint.xml` into the foundation's
      `build-logic`; delete the Softcover copies.

### 7 — Close out

- [ ] File every open entry of `../foundation-upstream-candidates.md` as an issue on the foundation repo; delete
      the file.
- [ ] Delete `../foundation-token-hygiene.md` (emptied by step 2).
- [ ] `.claude/rules/docs.md`: the "Something the foundation should adopt" row points at foundation issues.
- [ ] Softcover's submodule pointer sits on a tag; delete this directory and its `ACTIVE.md` line.

## Decisions

- Every app consumes the foundation as a submodule at `foundation/` via `includeBuild`; there is no published artifact.
- Submodule over subtree: the foundation keeps its own history, PRs and tags.
- An app merges to `main` only with its submodule pointer on a tagged foundation commit.
- Upstream agents, hooks and skills are generic; app-specific names, briefs and doc paths stay in the app.
- Each app enables the plugin per project from its own `foundation/` checkout.
- The agent layer goes upstream (step 2) before any app switches (step 3).
- Publishing is retired only after both apps build from the submodule (step 6 after step 5).

## Baseline

Filled in by step 3.

| Metric | Before | After |
|---|---|---|
| Configuration time | | |
| Cold `assembleDebug` | | |
