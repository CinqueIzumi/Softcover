---
name: observeasevents-captures-first-lambda
description: ObserveAsEvents is keyed on the flow only, so its lambda is the first-composition one; a remembered object captured in it goes stale when its remember keys change
metadata:
  type: feedback
---

`ObserveAsEvents(flow, onEvent)` (foundation designsystem-core) runs `LaunchedEffect(flow, lifecycle)`; the `onEvent` lambda is not wrapped in `rememberUpdatedState`. Anything the lambda captures from composition (a `remember`ed controller, `haptics`) is frozen at first composition.

**Why:** found in the shared progress-sheet slice review, where a `MarkAsReadController` (remembered with keys `slideDistancePx, playMotion, navPulse`) was hoisted into the ObserveAsEvents lambda. If a key changes the layout gets a new controller but the handler drives the old one, whose `runSlide` effect is cancelled, so the mark-as-read write never runs.

**How to apply:** when a change moves a call into an `ObserveAsEvents` lambda and the callee is a remembered object with keys, ask for `val current by rememberUpdatedState(obj)` inside the lambda.
