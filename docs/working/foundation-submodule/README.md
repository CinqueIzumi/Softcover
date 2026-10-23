# Foundation Submodule — Implementation Plan

> **Lifecycle.** Delete this directory in the PR that finishes step 3. The roadmap lives in GitHub Issues (see
> `CLAUDE.md` § Roadmap); this is a working plan for one rollout.

**Goal:** Softcover builds `rhaydus-foundation` from a git submodule at `foundation/`, pinned on the `v0.3.1` tag
its vendored docs match. The agent layer, the foundation guardrails, Maven Central publishing and the second app
are out of scope; they live in [`../foundation-upstream/README.md`](../foundation-upstream/README.md).

**Rollout model:** one Softcover branch, `chore/foundation-submodule` off `main`, one PR. Every step boundary
leaves the build and CI green. Foundation-side edits, if any turn up, go on `chore/softcover-submodule` in the
`rhaydus-foundation` checkout.

**Issue:** not filed yet — file a `kind:tech` issue (no milestone) when step 1 starts.

## Now

- **State:** tracker split; branches created in both repos; no code changed.
- **Next:** step 1 (submodule and build wiring).
- **Open questions:** none.
- **Verification:** not run.
- **Uncommitted:** this file and `../foundation-upstream/README.md`.

## How to run a step

1. Start a fresh session (`/clear`) and ask it to resume from this `## Now` block.
2. Do the one step `## Now` names.
3. Acceptance passes → tick the step below → update `## Now` → **ask the user before committing** (subject-only
   message) → `/handoff` → the user types `/clear`.

## Steps

| Step | Scope | Status |
|---|---|---|
| 1 | Submodule and build wiring | [ ] |
| 2 | Docs and rules from the submodule | [ ] |
| 3 | The plugin from the submodule | [ ] |

### 1 — Submodule and build wiring

One step, because CI breaks if the Gradle wiring lands without the submodule checkout.

- [ ] Measure configuration time and a cold `assembleDebug` on the Maven Central build; record under `## Baseline`.
- [ ] `git submodule add` the foundation at `foundation/`, checked out on `v0.3.1`.
- [ ] `settings.gradle.kts`: `includeBuild("foundation")` unconditionally, and `foundation/build-logic` in
      `pluginManagement`; delete the `foundation.local` switch and its `local.properties` key.
- [ ] `gradle/libs.versions.toml`: drop `rhaydusFoundation`; the `rhaydus-*` entries stay version-less, as
      substitution coordinates for the included build.
- [ ] CI (`ci.yml`, `release.yml`) checks out submodules; the iOS / Xcode build resolves them too.
- [ ] `README.md` clone instructions use `--recurse-submodules`.
- [ ] Measure again; record under `## Baseline`.

### 2 — Docs and rules

- [ ] Delete `docs/rhaydus/0.3.1/`; repoint `CLAUDE.md`, `docs/reference/**` and `.claude/rules/docs.md` at
      `foundation/docs/`; drop the vendored-docs line from `.claude/rules/docs.md`.
- [ ] Rewrite the Rhaydus block in `CLAUDE.md` by hand: the version line names the submodule and its tag, not
      `mavenCentral()` and the `foundation.local` switch. It stays a local override until the managed block
      lands upstream.
- [ ] `.claude/rules/foundation.md`, path-scoped to `foundation/**`: work on a branch inside the submodule, never
      a detached HEAD; push the foundation commit before moving the pointer; generic-only changes; ask before
      committing.
- [ ] `.claude/rules/build-wiring.md` gets the submodule build gotchas found in step 1.

### 3 — Plugin from the submodule

- [ ] Project-scoped marketplace pointing at `foundation/` in `.claude/settings.json`; drop the user-scope
      GitHub install.
- [ ] Confirm how plugin edits take effect (live, `/plugin update`, or a restart) and write it into
      `.claude/rules/foundation.md`.
- [ ] The four `Agent(rhaydus-kotlin:…)` deny rules, the duplicate `.claude/skills/style-check/` and the local
      hooks stay; removing them waits for the upstream agent layer.

## Decisions

- Softcover consumes the foundation as a submodule at `foundation/` via `includeBuild`.
- Submodule over subtree: the foundation keeps its own history, PRs and tags.
- Softcover merges to `main` only with its submodule pointer on a tagged foundation commit.
- Softcover switches before the agent layer goes upstream; the upstream work follows in its own tracker.
- Maven Central publishing stays live: the second app still consumes it.

## Baseline

Filled in by step 1.

| Metric | Before | After |
|---|---|---|
| Configuration time | | |
| Cold `assembleDebug` | | |
