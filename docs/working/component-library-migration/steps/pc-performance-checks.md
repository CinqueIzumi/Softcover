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
| PC-1 | Approved shape (below) — stop for approval | [ ] |
| PC-2 | Idle-frame check: a script that opens each main screen on a running emulator, waits for it to settle, and fails when it draws frames at rest | [ ] |
| PC-3 | Run PC-2 across every screen; fix or record each screen that redraws at rest | [ ] |
| PC-4 | Compose stability gate: compiler stability reports plus a baseline that fails on a newly unstable or non-skippable composable | [ ] |
| PC-5 | Fix or baseline what PC-4 reports | [ ] |

## PC-1 — decisions

- **Idle-frame check:** how it reaches each screen (deep links, a debug-only route list, or `adb input`),
  the settle time, the frame threshold, and whether it runs in CI (needs an emulator) or locally only.
- **Stability gate:** the Compose compiler's `reportsDestination` / `metricsDestination` with a checked-in
  baseline, or the stability-analyzer Gradle plugin; whether it joins `styleCheck`; whether it belongs in
  the foundation (`docs/working/foundation-upstream-candidates.md`).
- **Later, out of PC:** macrobenchmark for scroll and startup jank, plus baseline profiles.

## Approved shape

Not yet approved.
