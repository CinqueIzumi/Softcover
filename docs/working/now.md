# Now

The day-to-day working surface. This is the **only** planning doc you need open while working: what's getting attention right now, and the small things to clear ASAP. The long-horizon plan lives elsewhere and is unchanged — this file just points into it.

- **Focus** is the 1–2 topics being actively driven. Each is a pointer to its real place in the plan (`[[step]]` / roadmap tag), never a fork of it.
- **Incoming user requests** is a holding pen for asks that have no catalogue entry or step yet. Triage each into `idea-catalogue.md` + `roadmap-steps.md` before scheduling it.
- **Fast-track fixes** is a flat, unordered backlog of small things to do soon, independent of the release cadence. They don't wait for a phase slot.

**How this relates to the rest:**
- [idea-catalogue.md](idea-catalogue.md) — the idea catalogue (the *what*).
- [roadmap-steps.md](roadmap-steps.md) — the sequenced steps (the *order*).
- [release-plan.md](release-plan.md) — steps bundled into versioned drops (the *when*).
- [../../ROADMAP.md](../../ROADMAP.md) — the **public**, user-facing projection of the release plan.
- [foundation-upstream-candidates.md](foundation-upstream-candidates.md) — findings to push upstream into the nl.rhaydus foundation (bugs, reusability gaps, gates to promote).

**Maintenance rules.**
- A **focus** item is a step pulled to the front — link it to its step number / roadmap tag so it stays anchored. When it ships, it follows its step's normal lifecycle (deleted from `roadmap-steps.md`); remove it from Focus here too.
- An **incoming user request** is deleted from here the moment it has a catalogue tag and a step — it then lives in the plan like anything else, not in two places.
- A **fast-track fix** is one line, checked off and **deleted** when it ships (same discipline as `roadmap-steps.md`). When a fix ships, fold it into the next release's notes in `release-plan.md` (the "+ fixes and polish" line) rather than listing it individually on the public roadmap.

---

## Focus

_The 1–2 topics being driven right now. Each links to its step / roadmap tag._

### 3.1.1 — pickup list

_The agreed scope for the 3.1.1 drop, captured as a working checklist. None of these are triaged into [idea-catalogue.md](idea-catalogue.md) / [roadmap-steps.md](roadmap-steps.md) yet — do that (or fold them into the 3.1.1 entry in [release-plan.md](release-plan.md)) before they ship, so the public [ROADMAP.md](../../ROADMAP.md) stays a projection of the plan rather than of this file._

_Unlike a fast-track fix, an item here is **ticked rather than deleted** when it lands, and carries its catalogue tag once triaged: the list is this drop's scope, so it has to keep showing what is done against what is left. The whole block is deleted in one go when 3.1.1 ships._

- [x] **Horizontal shelf swiping in the Library** (`B.1.14`). Shipped as a `HorizontalPager` behind an opt-in "Swipe between shelves" toggle (Appearance → Display, default **off**), plus a shelf neighbour rail naming the shelves either side. The two-parallel-presentations worry did not materialise: the pager stays the content container with the toggle only flipping `userScrollEnabled`, so there is one mobile presentation either way.
- [x] **Clarify the top book on Explore** (`B.3.12`). The hero was the one section in the feed with no opener, so an "Arriving <date>" chip was all that hinted at what it was. It now names itself on its own top row with the DS §2.3 inline 20×1 hairline eyebrow — `MOST ANTICIPATED · NEXT 30 DAYS` — and the readers count beside the date badge reads "N readers waiting", so the eyebrow gives the window and the count gives the ranking basis: the most-shelved book releasing inside that window. The window is interpolated from `FEATURED_RELEASE_WINDOW_DAYS`, moved out of `SearchRemoteDataSourceImpl` into `domain/model/FeaturedReleaseWindow.kt` so the copy and the query it describes can't drift. A first pass gave the card the full `EditorialSectionHeader` (accent bar + headline + description sentence) like every rail below it, but that pushed it most of the way down the opening screen for a pick that rotates weekly; the inline register says the same thing in one line. Also fixed the 44dp gap under the search chrome (a leading `Spacer` inside a `spacedBy(36.dp)` column paid both). Mobile and desktop both.
- [x] **Search back-button handling** (`B.3.13`). Back is now a ladder on mobile Explore, one rung per press: a query or mood browse on screen clears (`OnClearSearchAction`, which drops the chrome's focus with the results, so leaving a typed search never costs more than one press), the focus surface alone closes (`OnSearchDismissedAction`), and the resting feed hands back to the shell. Two `NavigationBackHandler`s with mutually-exclusive `searchPhase` predicates, matching the Library's selection/rearrange shape; each dispatches the same action its on-screen control does, so nothing clears the field's focus from the screen side. One caveat left alone deliberately: while the keyboard is up the platform eats the first press to put it away, so a focused search costs that press before either handler sees anything. Desktop gets the same intent as a single rung — Esc clears the search (its field is persistent, so there is nothing beneath the clear) — via `dismissOnEscape`, held unconditionally enabled with the guard inside the callback: the modifier grabs focus on the disabled→enabled flip, and gating it on "a search is running" would flip it on the first typed character and yank focus out of the field. Filed as `F26` in [foundation-upstream-candidates.md](foundation-upstream-candidates.md) (a listen-without-taking-focus variant). All three call sites now read one derived `ExploreScreenUiState.hasActiveSearch` — the mobile back rung, the desktop Esc rung, and desktop's results-vs-discovery branch, which had been re-testing the fields by hand.
- [ ] **Tag picker "see more".** When choosing tags for a book, tapping "see more" should immediately reveal all of the user's previously chosen tags.
- [ ] **Exclude "unknown" from author stats.** Add a switch that drops books with missing/unknown author data from the author breakdown, so the percentages can be read without the unknown bucket skewing them.
- [ ] **Full deadline tracking back on the Library screen.** Restore the complete deadline treatment: "finish by X", how many pages you're behind, the pages/day needed to catch up, and the rest of that readout.

**Maybes** (in scope only if they turn out to be cheap):

- [ ] **Exclude books with no genre data from the genre stats.** Wanted, but currently the genre breakdown is computed from the aggregate query and it's unclear whether aggregates can express "has no genres at all". Investigate first: if it isn't expressible there, the fallback is a normal (non-aggregate) query, which is judged far too heavy — in that case, drop it. Shares its landing site (`toGenreSlices`) with Step 7.18 (full genre breakdown), which also reworks that aggregate — settle the two together rather than opening it twice.

> Foundation adoption onto local 0.3.0 is **done** (21 F-items landed). The only residue is the build-logic batch (F18/F20/F21), hard-blocked until the foundation publishes `build-logic` as Gradle plugins — tracked in [foundation-upstream-candidates.md](foundation-upstream-candidates.md), not here.

---

## Incoming user requests

_New asks that aren't in the plan yet. Each needs a catalogue entry (`idea-catalogue.md`) and a step (`roadmap-steps.md`) before it can be scheduled — this section is the holding pen, not their home. Delete a line once it's been promoted._

- _(nothing waiting — the reading-journal ask was triaged: the "log progress at a chosen date & time" half is now `B.2.12` / Step 2.15 (shipped in 3.1.0), and the edit/delete-entries half is `B.2.13` / Step 3.15 (3.6.0).)_

---

## Fast-track fixes

_Small things to clear ASAP, outside the release cadence. One line each; delete when shipped._

- [ ] Complete the AGP 9 migration: flip `android.builtInKotlin` / `android.newDsl` back to defaults in `gradle.properties` and drop the explicit `org.jetbrains.kotlin.android` plugin once KSP supports AGP 9's built-in Kotlin (currently blocked — see architecture-review B2). Verify the Room KSP path still works after the switch.
- [ ] Replace the deprecated `FlowRowOverflow.expandIndicator` overflow API in `ExpandableFlowRow` once Compose ships a maintained replacement (currently `@file:Suppress("DEPRECATION")`; the successor `ContextualFlowRow` is also deprecated, so there is no stable target yet).
- [ ] Silence the Room "Schema export directory was not provided" warning from `:core:database:kspAndroidMain`: the Room Gradle plugin (2.7.2) wires `room.schemaLocation` for the jvm/iOS KSP targets but not the AGP 9 KMP `androidLibrary` target, so only the android KSP run warns (the schema still exports correctly via the other targets and is committed). Revisit when the Room plugin recognises the new KMP android target, or wire the location to the android KSP without conflicting with the plugin's other-target wiring.
- [ ] Re-enable the three detekt rules the shared foundation baseline switches off — `UnreachableCode`, `IgnoredReturnValue`, `RedundantSuspendModifier` — once detekt 2.x (K2 frontend) is adopted, and re-triage. They are off only because detekt 1.23 embeds a Kotlin 1.9 frontend and, on a Kotlin 2.x codebase, all three report exclusively false positives (verified against the compiler). Reasons are documented in `detekt-rules/src/main/resources/config/detekt.yml`.
- [ ] Extend the crash-safety detekt gate to `iosMain`. `detektIosArm64Main` has no type resolution at all, so `rhaydus:UnguardedFlowTerminalRead` cannot run on iOS sources (no real `Flow` terminal reads live there today, so this is a coverage gap, not a live bug). Blocked on detekt supporting type resolution for native targets.
- [ ] Wire the foundation's own `detektCheck` into its `check` lifecycle. It is registered in `detekt-rules/build.gradle.kts` but attached to nothing, so the foundation never gates on the shared config it ships to every app.
- [ ] The 18 reading-session use cases in `core/personal/domain/usecase/` do not wrap their bodies in `runCatchingLogged`, so a repository throw (a Room I/O failure in `ReadingSessionRepositoryImpl.pause` / `resume` / `stop`) escapes to `ActiveSessionController`'s scope uncaught. The code-style rule says a use case wraps; these predate it. Fixing it changes their return types to `Result<T>` and touches every caller — hence a fix of its own, not a drive-by.
- [ ] **Audit the whole unit-test suite for coroutine-safety / test-dispatcher correctness** — a systemic false-positive source. Root cause found while building Hidden-suggestions: `DismissedContinueSeriesDaoTest` built Room with `setQueryCoroutineContext(UnconfinedTestDispatcher())` but ran each body in a bare `runTest {}` whose scheduler differs, so Room-`Flow` emissions dispatched on a clock nothing advances → Turbine `awaitItem()` hung the Gradle worker at 0% CPU (intermittently — it "passed" on lucky eager-dispatch runs). Many other tests likely dispatch on `Dispatchers.Main` without `setMain`, or otherwise don't share one `TestDispatcher`/scheduler, so they pass by luck. Go file-by-file: flag coroutine-safe vs pass-by-luck, fix to the shared-dispatcher pattern (one `TestDispatcher` field used as every dispatcher/`queryContext` + `Dispatchers.setMain`/`resetMain` + `runTest(testDispatcher)`), and verify each with a **bounded** run (a `--tests` filter past ~90s = a scheduler-mismatch hang). The test-writing agent guidance was updated (`.claude/agent-memory/rhaydus-kotlin-unit-test-writer/feedback_coroutine_safe_tests.md`); once the audit confirms the pattern, promote it into the shared `rhaydus-kotlin` `unit-test-writer` agent definition in the foundation.
- [ ] Fix the `styleCheck` red on `:feature:book_detail:detektAndroidMain` — `LongParameterList` on `BookDetailScreenScreenModel`'s constructor (`:41`), which takes ~28 params (26 use cases, each forwarded verbatim into `BookDetailDependencies`). Determine first *why* it fires: the foundation baseline sets `constructorThreshold: 30` precisely because TOAD ScreenModels inject many use cases via Koin, but Softcover's `config/detekt/detekt.yml` override declares only `functionThreshold` — so either the redesign genuinely pushed the count past 30, or the app-level override is shadowing the baseline's constructor relaxation. A config-layering bug is a config fix; a genuine overrun means grouping the use cases into cohesive parameter objects (shelf / lists / deadlines / review / tags / preferences) rather than raising the threshold. Pre-existing, unrelated to the profile redesign.
- [ ] Move the featured-release date range out of `SearchRemoteDataSourceImpl` and into `GetFeaturedUpcomingReleaseUseCase`, which should compute `today`/`maxDate` from an injected `Clock` + `TimeZone` the way `ObserveRecentReadingActivityUseCase` already does — the window is a selection rule, and a data source reaching for `Clock.System` itself can't be tested without one. Blocked on a prerequisite: `single<Clock>` / `single<TimeZone>` are declared only in **`:core:profile`'s** Koin module, so any other module resolving them is depending on a binding a sibling feature happens to register; promote those two bindings to a shared core module first. Touches the repository + data-source signatures and their three test files.
- [ ] Delete `LegacySecureApiKeyStorage` and its two impls (`AndroidLegacySecureApiKeyStorage`, `IosLegacySecureApiKeyStorage`) once every install has passed through a build carrying the F9 migration, along with the `legacySecureStorage` parameter on `ApiKeyLocalDataSourceImpl` and its tests. They exist only to carry the API key out of Softcover's pre-foundation Keystore/Keychain locations; a user who skips the release simply re-authenticates.
