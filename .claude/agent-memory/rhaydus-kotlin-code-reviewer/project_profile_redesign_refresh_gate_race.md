---
name: project_profile_redesign_refresh_gate_race
description: RefreshUserProfileDataUseCase + ProfileRefreshGate are shared across feature/reading AND feature/profile — a real (not hypothetical) concurrency surface for the once-per-session gate.
metadata:
  type: project
---

`RefreshUserProfileDataUseCase` (in `core/profile`) is injected into and called from **two independent
ScreenModels**: `feature/profile/.../ProfileScreenScreenModel` (via `UserInformationCollector.onLaunch`)
and `feature/reading/.../ReadingScreenScreenModel` (via its own `init { startInitializers() }` /
collector). Both call it as fire-and-forget: `dependencies.launch { refreshUserProfileDataUseCase() }`.

**Why this matters:** the Profile 1a redesign (see `[[architecture_profile_reading_life_phase01]]`)
added a process-lifetime `ProfileRefreshGate` (`var wasRefreshedThisSession: Boolean`, no lock) meant
to cap the profile network fetch at once per app session. Because Reading and Profile are separate
screens that can both mount early in a session (e.g. app opens on the Reading tab, user taps to
Profile shortly after), the gate's check-then-act (`if (gate.wasRefreshedThisSession) return; ...;
gate.markRefreshed()`) is a real, reachable race, not just a theoretical one — two overlapping
coroutines can both observe `false` and both fire the network fetch.

**How to apply:** when reviewing `ProfileRefreshGate` or any future once-per-session/debounce gate in
this codebase, check for a `Mutex`/atomic guard around the check-then-act, not just presence of a
boolean flag. When reviewing either `ReadingScreenScreenModel` or `ProfileScreenScreenModel`, remember
they share this singleton — a change to one screen's refresh timing can affect the other.

**Resolved 2026-07-17** (reading-life/trackedYears fix-round review): `ProfileRefreshGate.runOnce`
now wraps the whole check-then-act in `mutex.withLock { if (wasRefreshedThisSession) return@withLock;
block(); wasRefreshedThisSession = true }` — the race described above is closed. Confirmed by reading
the current file; no longer flag this as an open issue, only as a worked example of the fix if a
similar gate needs one elsewhere.
