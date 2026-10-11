# PC — Performance checks

**Stage:** PC — app-wide, any session before S12. **Delegation:** user (PC-1), `softcover-implementer`
(PC-2 onward).

Nothing catches a screen that keeps drawing while idle, or a composable that can no longer skip. Reading,
Library and Explore drew about 60 frames a second at rest: a hidden pull-to-refresh indicator looped
forever, and the wavy `ProgressBar` moved its wave. Both were found by hand with `dumpsys gfxinfo`
and a Perfetto trace. PC turns that check, and a Compose stability check, into repeatable gates.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| PC-1 | Approved shape (below) — stop for approval | [x] |
| PC-2 | Idle-frame check: dropped; no automated idle check (neither a debug route plus script nor Compose UI idle tests) | [x] |
| PC-3 | One-off `dumpsys gfxinfo` sweep: every screen idles at 0 frames except any `EmptyState` (~60 fps from `quoteGlyphSway()`), kept by choice since empty states are short-lived | [x] |
| PC-4 | Compose stability gate: skydoves analyzer 0.7.5, `androidMain` baselines in 15 modules, `stabilityCheck` in `check` and CI | [x] |
| PC-5 | Fix or baseline what PC-4 reports | [ ] |

## PC-1 — decisions

- **Idle-frame check:** how it reaches each screen (deep links, a debug-only route list, or `adb input`),
  the settle time, the frame threshold, and whether it runs in CI (needs an emulator) or locally only.
- **Stability gate:** the Compose compiler's `reportsDestination` / `metricsDestination` with a checked-in
  baseline, or the stability-analyzer Gradle plugin; whether it joins `styleCheck`; whether it belongs in
  the foundation (`docs/working/foundation-upstream-candidates.md`).
- **Later, out of PC:** macrobenchmark for scroll and startup jank, plus baseline profiles.

## Approved shape

- Stability: skydoves Compose Stability Analyzer plugin (`stabilityDump` baseline per module, `stabilityCheck` fails on a new unstable parameter); verify KMP / Kotlin 2.3.21 support first, else compiler reports plus a hand-written baseline diff task.
- Checks unstable parameters only, not skippability (strong skipping is on by default).
- Applied through `KmpComposeConventionPlugin` / `AndroidComposeConventionPlugin`; its own task wired into `check` and `ci.yml`, not `styleCheck`.
- Filed in `foundation-upstream-candidates.md` as a gate for build-logic; built here first.
