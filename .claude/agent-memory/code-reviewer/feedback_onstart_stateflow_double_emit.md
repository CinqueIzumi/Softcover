---
name: feedback_onstart_stateflow_double_emit
description: Flow.onStart{} guard combined with collecting a StateFlow double-fires on startup — a concrete bug found and empirically confirmed in rhaydus-foundation Batch H (offline-sync)
type: feedback
---

**Pattern to flag in review: `someStateFlow.onStart { if (someStateFlow.value) doThing() }.onEach { if (it) doThing() }.collect()`.**

A `StateFlow` always replays its current value to a brand-new collector (it's conflated/hot). So if
code adds an `onStart {}` block that manually re-checks the current value and does a side effect
"because the flow might not fire immediately," and the *same* side effect is also triggered from
`onEach` for `true` values, the side effect runs **twice** on startup when the flow is already in the
"true" state: once from `onStart`'s manual check, once from `onEach` handling the StateFlow's replayed
initial value. `onStart` does not suppress or interact with the real upstream collection — it just runs
its block *before* collection begins.

**Why this matters**: found in `rhaydus-foundation` Batch H (`offline-sync` module,
`DefaultOfflineWriteDrainer.start()`, release/0.3.0). The doc/KDoc describes the intended behavior as
"drains on connectivity return, **and once immediately if already online**" — the fix should be to drop
the `onStart` guard entirely and rely on the `StateFlow`'s own replay (that alone already satisfies
"drains once immediately if already online"), OR keep the guard but skip the first `onEach` emission
(e.g. `.drop(1)` after the onStart-driven initial drain) — not both mechanisms firing independently.

**How to apply / how to verify before flagging as confirmed (not just theoretical)**: don't just reason
about it — instrument and run. Add a call counter to the fake/mocked dependency the guarded side effect
reads from (e.g. a `getPendingCallCount` on a test double), add one assertion to an existing "already
online at start" test, run the single test via the project's `androidHostTest`/JVM test task, observe
the actual vs. expected count, then **revert the instrumentation** (`git checkout -- <test file>`) so
review artifacts don't leak into the diff. This turned a plausible-sounding "double drain" theory into a
confirmed `expected:<1> but was:<2>` failure in about two tool calls — cheap and removes any doubt before
reporting a 🔴/🟡 finding to the user.

**Gradle task name note (rhaydus-foundation, KMP `androidLibrary` target, no `assembleDebug`/app
variant)**: the JVM host-test task for this kind of module is `:<module>:testAndroidHostTest`, not
`:<module>:testDebugUnitTest` (that task doesn't exist here — this isn't an Android application module).
Run `./gradlew :<module>:tasks --all | grep -i test` first if unsure of the exact task name for a KMP
library module.
