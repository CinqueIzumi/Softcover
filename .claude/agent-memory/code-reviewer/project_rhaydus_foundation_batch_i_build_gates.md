---
name: project_rhaydus_foundation_batch_i_build_gates
description: rhaydus-foundation Batch I (F18/F20/F21/F23) review findings — buildHealth not wired to check; checkModuleGraph config-cache risk
type: project
---

Batch I (release/0.3.0, staged as of 2026-07-03) added four build gates to rhaydus-foundation:
F23 no-raw-logging ktlint rule, F21 LintConvention dedup, F18 module-graph gate, F20 buildHealth policy.
Reviewed for correctness/idiom/docs (not a build check — build was independently verified green by the user).

**Finding 1 (real gap, not yet fixed as of this review): `buildHealth` is NOT wired into `check`/`build`.**
Only `checkModuleGraph` (via `ModuleGraphConventionPlugin.kt` `dependsOn(checkModuleGraph)` on every
subproject's `check`) is actually gated. `com.autonomousapps.dependency-analysis`'s `buildHealth` is a
root-only aggregate task with no `dependsOn` wiring anywhere in `build.gradle.kts`, and
`.github/workflows/publish.yml` only runs `./gradlew build` (no separate `buildHealth` step). Verified via
`./gradlew :core-common:check --dry-run` — `checkModuleGraph`/`verifyPluginVersion` appear in the task
graph, no health task does. But `docs/CAPABILITIES.md` and `docs/architecture.md` §7 both claim "Both
[module-graph and buildHealth] wire into every module's `check`" — that claim is false for buildHealth.
**Why it matters:** an unused-dependency or wrong-api-exposure regression will pass CI silently; the
policy is a manually-run report, not an enforced gate, despite being documented and framed as one.
**How to apply:** when reviewing rhaydus-foundation build-gate work, don't take "gate passes" at face
value — check whether the gate is actually reachable from `check`/`build`/CI, not just runnable directly.

**Finding 2 (latent, not currently triggered): `checkModuleGraph`'s `doLast` is not configuration-cache-safe.**
It captures `target` (root `Project`) directly and reads `target.subprojects`/live `Configuration` objects
at execution time. Config cache isn't enabled in this repo today so it doesn't fail, but it's inconsistent
with `verifyPluginVersion` in the same `build.gradle.kts`, which deliberately captures snapshotted local
vals instead of a `Project` reference. Worth a comment or refactor if config cache is ever turned on.

Otherwise Batch I was solid: NoRawLoggingRule's AST qualifier-receiver logic is correct (verified against
actual PSI shape for `println`/`System.out.println`/`Log.d`/`android.util.Log.w`); LintConvention.kt is a
clean dedup; ModuleGraphExtension's api-edge detection (`endsWith("api") && !contains("test")`) and tier
config were verified correct against the real module graph; the `ksafe` buildHealth exclusion is correctly
scoped (private ctor param, not on the public API); Batch H's `DefaultOfflineWriteDrainer.kt`/test diff was
confirmed purely mechanical rewrapping, no logic change.

See also [[project_rhaydus_foundation_upstream_migration]].
