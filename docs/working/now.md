# Now

The day-to-day working surface. This is the **only** planning doc you need open while working: what's getting attention right now, and the small things to clear ASAP. The long-horizon plan lives elsewhere and is unchanged — this file just points into it.

- **Focus** is the 1–2 topics being actively driven. Each is a pointer to its real place in the plan (`[[step]]` / roadmap tag), never a fork of it.
- **Fast-track fixes** is a flat, unordered backlog of small things to do soon, independent of the release cadence. They don't wait for a phase slot.

**How this relates to the rest:**
- [idea-catalogue.md](idea-catalogue.md) — the idea catalogue (the *what*).
- [roadmap-steps.md](roadmap-steps.md) — the sequenced steps (the *order*).
- [release-plan.md](release-plan.md) — steps bundled into versioned drops (the *when*).
- [../../ROADMAP.md](../../ROADMAP.md) — the **public**, user-facing projection of the release plan.
- [foundation-upstream-candidates.md](foundation-upstream-candidates.md) — findings to push upstream into the nl.rhaydus foundation (bugs, reusability gaps, gates to promote).

**Maintenance rules.**
- A **focus** item is a step pulled to the front — link it to its step number / roadmap tag so it stays anchored. When it ships, it follows its step's normal lifecycle (deleted from `roadmap-steps.md`); remove it from Focus here too.
- A **fast-track fix** is one line, checked off and **deleted** when it ships (same discipline as `roadmap-steps.md`). When a fix ships, fold it into the next release's notes in `release-plan.md` (the "+ fixes and polish" line) rather than listing it individually on the public roadmap.

---

## Focus

_The 1–2 topics being driven right now. Each links to its step / roadmap tag._

- Foundation adoption onto **local 0.3.0** — **18 F-items landed & committed** (core-common, the designsystem-core components/seams, toad convention, ktlint gates, F3 NavPulse, F2 + F3's `BottomBarScaffold`, F1/F19/F22 type-resolved detekt), all green. Remaining (F9/F10→F8 core-platform+offline-sync, F18/F20/F21 build-logic) each need a call/verification (iOS build, plugin publishing). Details: [foundation-upstream-candidates.md](foundation-upstream-candidates.md) → "✅ Adoption progress — LANDED".

---

## Fast-track fixes

_Small things to clear ASAP, outside the release cadence. One line each; delete when shipped._

- [ ] Complete the AGP 9 migration: flip `android.builtInKotlin` / `android.newDsl` back to defaults in `gradle.properties` and drop the explicit `org.jetbrains.kotlin.android` plugin once KSP supports AGP 9's built-in Kotlin (currently blocked — see architecture-review B2). Verify the Room KSP path still works after the switch.
- [ ] Replace the deprecated `FlowRowOverflow.expandIndicator` overflow API in `ExpandableFlowRow` once Compose ships a maintained replacement (currently `@file:Suppress("DEPRECATION")`; the successor `ContextualFlowRow` is also deprecated, so there is no stable target yet).
- [ ] Investigate and fix an HttpClient crash a user reported when starting the **Linux desktop client** (JVM/desktop target). Reproduce on Linux, identify the failing Ktor/HttpClient path, and fix in this version.
- [ ] Silence the Room "Schema export directory was not provided" warning from `:core:database:kspAndroidMain`: the Room Gradle plugin (2.7.2) wires `room.schemaLocation` for the jvm/iOS KSP targets but not the AGP 9 KMP `androidLibrary` target, so only the android KSP run warns (the schema still exports correctly via the other targets and is committed). Revisit when the Room plugin recognises the new KMP android target, or wire the location to the android KSP without conflicting with the plugin's other-target wiring.
- [ ] Re-enable the three detekt rules the shared foundation baseline switches off — `UnreachableCode`, `IgnoredReturnValue`, `RedundantSuspendModifier` — once detekt 2.x (K2 frontend) is adopted, and re-triage. They are off only because detekt 1.23 embeds a Kotlin 1.9 frontend and, on a Kotlin 2.x codebase, all three report exclusively false positives (verified against the compiler). Reasons are documented in `detekt-rules/src/main/resources/config/detekt.yml`.
- [ ] Extend the crash-safety detekt gate to `iosMain`. `detektIosArm64Main` has no type resolution at all, so `rhaydus:UnguardedFlowTerminalRead` cannot run on iOS sources (no real `Flow` terminal reads live there today, so this is a coverage gap, not a live bug). Blocked on detekt supporting type resolution for native targets.
- [ ] Wire the foundation's own `detektCheck` into its `check` lifecycle. It is registered in `detekt-rules/build.gradle.kts` but attached to nothing, so the foundation never gates on the shared config it ships to every app.
- [ ] The 18 reading-session use cases in `core/personal/domain/usecase/` do not wrap their bodies in `runCatchingLogged`, so a repository throw (a Room I/O failure in `ReadingSessionRepositoryImpl.pause` / `resume` / `stop`) escapes to `ActiveSessionController`'s scope uncaught. The code-style rule says a use case wraps; these predate it. Fixing it changes their return types to `Result<T>` and touches every caller — hence a fix of its own, not a drive-by.
