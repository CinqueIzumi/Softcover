# S11 — Contract retrofit (R10 + R11) and premium-feel contracts

**Stage:** S11 — sweeps every call site that builds a UI model in composition, or passes a loose
render parameter, then builds the premium-feel touches that belong in shared components, before the
migration's final verification. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

**Completion bar:** `component-contract.md` § 7.4a ends with only its standing exceptions (data-free
primitives among them), and every premium-feel sub-step has landed.

## Sub-steps

| Sub | Scope | Runs after | Status |
|---|---|---|---|
| S11-1a | The `UiModelBuiltInRender` gate and `@ModelBuiltInComposition`, inactive; findings recorded as the work list | — | [x] |
| S11-1b | `:core:component` sheets: `ChooseListsBottomSheet` (`membershipChipFor`), `ShareCardSheetContent` (`scaffold.copy`, `SheetFooterUiModel.Inline`), `VerdictSheet` (`spoilers.copy`, `editorBufferToDocument`) | S11-1a | [x] |
| S11-1c | Library: `LibraryControlLine` (`rearrangeChipFor`, `filterPillFor`), `LibraryScreenLayout.{jvm,mobile}` (`mastheadFor`) | S11-1a | [x] |
| S11-1d | Profile: the preview-state properties in `ProfileScreenLayout.{jvm,mobile}` move to fixtures | S11-1a | [x] |
| S11-1e | Item 4's sheet bodies the gate misses, swept with their owners | S11-1a | [x] |
| S11-1z | Turn `UiModelBuiltInRender` on; add it to `component-contract.md` § 7.4 and the intro's gate count, drop R10's "once the sweep enables it" | S11-1b–e | [x] |
| S11-2 | R11 holdouts | — | [x] |
| S11-3 | `TopBarSurface`, the `ShareCardPalette` fold, the final § 7.4a check | S11-1, S11-2 | [x] |
| S11-5 | Provide `LocalHaptics` at the theme root; one way of reading haptics | — | [x] |
| S11-6 | Haptic vocabulary: `commit` for milestones only, fired after the outcome, `reject` on failure | S11-5 | [x] |
| S11-41 | Outcome haptics on the remaining mutations and failure paths | S11-6 | [x] |
| S11-9 | Explore search keeps its results while a refresh runs | SL-15c | [x] |
| S11-11 | Numbers that change render through `StatNumber` | S11-2 | [x] |
| S11-12 | The `StatNumber` count-up starts when the mask clears | SL-15c, S11-11 | [ ] |
| S11-13 | Charts grow, morph and slide | SL-15a | [ ] |
| S11-14 | Cover selection animates | SL-15a | [ ] |
| S11-15 | One reorder lift treatment | S11-6 | [ ] |
| S11-16 | Selectable controls fire `select`; `ToggleRow` shares its interaction source | S11-6 | [ ] |
| S11-17 | Selection states animate with motion-scheme specs | SL-15a | [ ] |
| S11-18 | `SheetScaffold` footer: no haptic of its own, spinner on the running button | S11-6 | [ ] |
| S11-19 | The progress sheet plays its dismiss on submit | — | [ ] |
| S11-20 | Every sheet host leaves on `Dismissed` | S11-19 | [ ] |
| S11-21 | Panel-form `AdaptiveModalSheet` animates in and out, with elevation | SL-15a | [ ] |
| S11-22 | `TextFieldUiModel` error state: outline, shake and `reject` | S11-6 | [ ] |
| S11-23 | `StepperCircle`: press scale, a tick per step, accelerating auto-repeat | S11-6 | [ ] |
| S11-24 | Progress-sheet tab body crossfades with a size transform | SL-15a | [ ] |
| S11-25 | Add-to-list: bookmark, chip and spinner animate; `select` fires | S11-6 | [ ] |
| S11-26 | Search chrome: press, border, slot crossfade, scroll lift, Refresh text | SL-15a | [ ] |
| S11-27 | Desktop hover on every clickable primitive; sidebar press scale | — | [ ] |
| S11-28 | Desktop share and save say what happened | — | [ ] |
| S11-29 | Share-card success and failure feedback | S11-6, S11-28 | [ ] |
| S11-30 | The update callout crossfades its call to action | S11-6 | [ ] |
| S11-31 | The update download shows determinate progress | S11-30 | [ ] |
| S11-32 | Tab switches take their specs from the motion scheme | SL-15a | [ ] |
| S11-33 | Explore pull-to-refresh moves onto `PullToRefreshEyebrow` | S11-5 | [ ] |
| S11-34 | Re-tapping the current tab scrolls to the top or pulses | — | [ ] |
| S11-35 | Exact chart values on hover and press-and-scrub | S11-13 | [ ] |
| S11-36 | One root push/pop transition | SL-15a | [ ] |
| S11-37 | The Focus Mode cover travels | S11-36 | [ ] |
| S11-38 | The full-screen cover grows out of the hero | S11-36 | [ ] |
| S11-39 | Shared title and rating keys into book detail | S11-36 | [ ] |
| S11-40 | Predictive back drives the push/pop transition | S11-36–S11-39 | [ ] |
| S11-C | Convergence pass over the family (family-procedure.md § Phase 3) | all | [ ] |

## Approved shape

S11-11:
- `StatNumberUiModel` gains `prefix: String = ""` and `suffix: StatNumberSuffix? = null` (`Fixed(text)` | `Plural(one, other)`, picked by the displayed integer); the phrase stays one tabular `SoftcoverText`, only the digits move; the stand-in is prefix + digit stand-in + suffix.
- `StatNumberFormat.Duration` renders seconds as h:m, for audiobook positions.
- New variants, one per face: `Hero` (`statHero`), `Headline` (`headlineSmall`), `BodyStrong` (`bodyMedium` Medium), `Body`, `BodySmall`, `Label` (`labelSmall`); ink stays `LocalContentColor`.
- The hairline pulse is removed entirely; the existing default spring stays (it matches `ProgressBar`'s); KDoc says "springs".
- Reading `FeaturedProgressUiModel`: `percent` (`Headline`, "%") and `pageLead` (`BodyStrong`, "Page " or `Duration`) become `StatNumberUiModel`; the faded tail stays text beside it; built in `toFeaturedProgress`.
- Book detail `InProgressUiModel.percent` becomes `StatNumberUiModel` (`Hero`, "%"); built in `toInProgress`.
- `ShelveControlCard`'s `readingTrailingStatus` leaves composition: a collector-built `BookDetailUiState` field of two `StatNumberUiModel`s (percent "%", position " · p. " or `Duration`), `Label`.
- `StreakStripUiModel.runningLabel: String?` becomes `running: StatNumberUiModel?` (`BodySmall`, `Plural(" day running", " days running")`); built in `streakStripFor`.
- `SelectionHeader`'s loose `selectedCount: Int` becomes `LibraryUiState.selectionCount: StatNumberUiModel` (`Headline`, " selected").
- `PageMastheadUiModel.subtitle` becomes a sealed subtitle: `Text(String)` | `Counts(titles, pages?)`; the mobile `MastheadHeader` takes it in place of `subtitle` + `isSubtitleLoading`; built in `MastheadSnapshot.subtitleFor`.
- "Show N titles" waits for foundation F42 (a `RhaydusButton` content slot); it stays plain text until then.
- Gallery fixtures for prefix, `Plural`, `Duration` and loading; the `components.md` index line updated.

## R10 edges

S11 settles the three R10 edges `component-contract.md` names. One stays open on purpose, as a recorded
exception rather than a holdout: the preview fixtures / Component Gallery, which construct models by
definition. The library-owned-copy edge is settled by the `OfflineCopy` seam; the composition-only-value
edge by R10's one-model-per-outcome rule.

## S11-3 — `TopBarSurface`, `ShareCardPalette`, final check

- Settle book detail's scroll-derived `TopBarSurface`, and with it R10's composition-only-value edge:
  either the rule for it goes into `component-contract.md` R10, or it becomes a § 7.4a exception.
- Fold `ShareCardPalette`'s two parallel colour `when`s into a per-variant lookup.
- Run the final `component-contract.md` § 7.4a check: no remaining exception rows.

## S11-5 to S11-41 — Premium feel

Approved in the premium-feel triage. The app keeps today's UI until these land, so each one is a
deliberate change the S12 walkthrough expects. Rules for every sub-step:

- All motion goes through `playDecorativeMotion()` and takes its specs from `MaterialTheme.motionScheme`,
  never a hand-written `tween`.
- Haptics fire on Android and iOS only; desktop `Haptics` is a no-op. Keep them rare.
- Build on what exists: press scale on the clickable primitives, `NavPulse`, `staggeredEntry`,
  `mutationAnimated`, `SkeletonCrossfade`, `bookCoverTransitionKey`, `MarkAsReadBurst`, the animated
  sheet closes. Never add a second system beside one.
- Each scope names the moment and the touch; Phase 1 designs the API.

### S11-5 — Haptics actually fire

`LocalHaptics` defaults to `NoOpHaptics` and nothing provides it, so every call site that reads it
(settings tab reorder, library selection, edition and book reorder, bulk-action sheets, the sidebar
threshold) is silent. Provide the platform haptics once at the theme root and read them one way across
the app.

### S11-6 — Haptic vocabulary

`commit` is spent on library moves, Remove, list toggles and Focus Mode saves, and fires before the
outcome is known; `threshold` fires on session start and stop. Keep `commit` for finishing a book,
creating a list and a session recap. Use `select` for routine changes and nothing on Remove. Fire after
the outcome, and `reject` on failure.

### S11-41 — Outcome haptics everywhere else

Each action below sends a success / failure event; the screen fires `select` / `reject` from it.

- Rating and review saved (`OnSaveVerdictAction`, book detail and reading).
- Edition switched (`OnNewEditionSaveClickAction`, `OnUpdateToScannedEditionClickAction`).
- Deadline set (`OnDeadlinePickedAction`); clearing it stays silent.
- List toggled on book detail (`OnToggleListMembershipAction`).
- Owned flag (`OnEditionOwnedToggleAction`).
- Want to Read from Explore (`OnAddBookToLibraryClickAction`).
- Barcode scan (`BarcodeScannerScreen`): `select` on a resolved ISBN; `reject` on an invalid one or a
  failed lookup or add.
- `reject` only: tag save rolled back (`UserTagActionSupport`), list reorder or ranked toggle rolled
  back (`OnReorderListBooksAction`, `OnSetListRankedAction`), library tab visibility save failed
  (`OnSaveLibraryVisibilityAction`), log-out failed (`OnLogOutClickAction`).
- Onboarding (`OnApiKeySaveClickAction`): `commit` on sign-in, `reject` on a rejected key.

Stays silent: star rating (`StarRatingInput` already ticks), refresh and sync completion, every
remove, delete, undo, unblock and dismiss, draft-only sheet edits, per-tag and shelf-reorder success.

### S11-9 — Search keeps its results

Each keystroke and sort change (`OnQueryChangeAction`, `OnSortModeChangeAction`,
`BookCardModelsCollector`) replaces the results with stand-ins until the debounce lands. While a refresh
runs, keep the old results (dimmed, or with only the field spinner) and crossfade to the new ones.
Stand-ins show for the first query only.

### S11-11 — Numbers that change

Reading's hero percentage and page (`FeaturedProgressStat`), book detail's percentage
(`InProgressSection`, `ShelveControlCard`), the streak (`StreakStrip`), and "N selected" / "Show N
titles" / "N titles" (`SelectionHeader`, `LibraryDisplayDerivation`, `LibraryStats`) are plain text
beside gliding bars. Map each to `StatNumberUiModel` and render it with `StatNumber`.

### S11-12 — Count-up after the mask

Profile's hero totals roll during the skeleton's hold, and the tick draws while masked and becomes a
solid line on large values. Start the roll when the mask clears; keep the tick a tick on large ranges.

### S11-13 — Charts in motion

`ColumnChart`, `RankedBars` and `ProportionChart` jump to size on load, on Books/Pages and on "hide
untagged", and the eight loading columns swap out at once. Bars grow from the baseline, morph between
metrics and slide to new proportions; labels and caption crossfade with them.

### S11-14 — Cover selection

The dim, ring, tint and mark (`BookCardParts`, `SelectionMark`) switch instantly, and entering
selection dims every cover in one frame. Animate the dim, ring and fill; the check pops in.

### S11-15 — Reorder lift

Dragged covers (`BookList`, `EditionList`) ignore `isDragging`, and settings tabs (`ReorderableTabsGroup`,
`ReorderableRow`) only change tint. One reorder treatment: scale and shadow while dragged, settling on
drop, and a `tickle` per slot crossed.

### S11-16 — Selection ticks

Only `SegmentedControl` fires `select`. Fire it on every selection change in `Chip`, `SheetRow`,
`ToggleRow`, `PreviewTile` and the settings check rows. `ToggleRow` shares its interaction source with
its `Switch`, so the thumb grows on a row press.

### S11-17 — Selection states animate

`SheetRow` ink, the `PreviewTile` ring, the settings check and the `Toggle` glyph swap in one frame, and
`Chip` hand-writes its timing. Animate them with motion-scheme specs; the `Toggle` glyph crossfades and
pops when it fills.

### S11-18 — Sheet footer commit

A secondary button's busy state spins `SheetScaffold`'s primary. The spinner goes on the button that
is running and fades in. The footer fires no haptic of its own; the screen fires the outcome haptic
from the save's event, so saving a rating gets `select`.

### S11-19 — Progress sheet dismiss

Save and "Mark as read" (`OnUpdatePageProgressClickAction`, the percentage and time variants,
`ReadingOverlays`) clear `showProgressSheet`, so the host removes the sheet before its animated `hide()`
runs. Submitting plays the dismiss before the host clears its state, the way `dismissesSheet` footer
buttons do.

### S11-20 — Hosts leave on `Dismissed`

A host that navigates away on a sheet click or a `closeRequested` write removes the sheet mid-animation.
Sweep every `SheetScaffold` and `AdaptiveModalSheet` host: each leaves on `SheetScaffoldEvent.Dismissed`.

### S11-21 — Panel sheets animate

The panel form of `AdaptiveModalSheet` (share, verdict, lists, re-auth, unknown ISBN on tablet and
desktop) is a bare `Dialog`: no enter or exit, an instant dismiss, a 1 dp outline and no elevation. Scale
and fade in, fade the scrim, and animate the close before `onDismissRequest`, matching the compact sheet;
Esc plays the same exit; add elevation. The foundation part is F35 in
`../../foundation-upstream-candidates.md`.

### S11-22 — Field errors on the field

A rejected API key (`OnApiKeySaveClickAction`), a taken list name (`CreateListSheetContent`), a page past
the end (`OnPageDraftChangedAction`, which silently clamps 9999 to the page count) and an invalid ISBN
(the barcode scanner) only raise a snackbar, because `TextFieldUiModel` has no error hook. Add an error
state to the field model with an outline and a shake (`shakeOnError`), which brings `reject`.

### S11-23 — Steppers

`StepperCircle` only ripples, so 280 to 312 takes 32 taps. Press scale, a `tickle` per step, and
long-press auto-repeat that speeds up.

### S11-24 — Progress-sheet tabs

`UpdateProgressBottomSheet` switches its tab body with a plain `when`, so the sheet height jumps.
Crossfade the body with a size transform.

### S11-25 — Add to list

In `ChooseListsBottomSheet` the bookmark swaps instantly and the spinner swaps through `alpha(0/1)`. The
bookmark fills with a pop, the chip and spinner crossfade, and `select` fires.

### S11-26 — Search chrome

`SearchTopBar`'s barcode button only ripples, its border and spinner/× slot pop, and nothing separates the
bar from scrolled content; `MastheadRefreshAction`'s desktop text swaps in one frame. Press scale on the
barcode button, animate the border, crossfade the slot, lift the bar when content scrolls under it, and
crossfade the Refresh text.

### S11-27 — Desktop hover

Cover cards hard-code `hoverHighlight = false`, the primitives show a hand cursor and nothing else, and
the shelf sidebar strips the ripple without adding press scale. Pair `hoverHighlight` with
`pointerHandCursor` in every clickable primitive and let cover cards lift the cover. The sidebar gets
press scale and an animated selection.

### S11-28 — Desktop share and save

On desktop, Share copies to the clipboard without saying so, cancelling the save dialog reports a
failure, and the labels say "gallery" (`ShareCardSheetContent`). Confirm "Copied to clipboard", say
nothing on a cancelled save, and use desktop labels. The foundation part is F36 in
`../../foundation-upstream-candidates.md`.

### S11-29 — Share-card feedback

Saving or sharing a card succeeds with a snackbar or a silent close. `commit` and a brief settle on the
preview on success; `reject` and a shake on failure.

### S11-30 — Update callout

`Callout`'s call to action swaps in one frame between update states. Crossfade it; `commit` on
Downloaded, `reject` on Failed.

### S11-31 — Update download progress

The update download (`AppUpdateDataSourceImpl`, `AppUpdateCalloutMapper`) reports no progress. Carry the
bytes downloaded and show a determinate bar, on Android and desktop.

### S11-32 — Tab-switch timing

`BottomBarScreen` hand-writes `tween(200)` for tab switches. Take the specs from
`MaterialTheme.motionScheme`.

### S11-33 — Explore pull-to-refresh

Explore (`EditorialContent`) uses a Material `IndicatorBox` with no tick. Move it onto
`PullToRefreshEyebrow`, which brings the `threshold` tick the other refresh hosts fire.

### S11-34 — Re-tap the current tab

Re-tapping the current tab in `BottomNavigationBar`, `NavigationRailBar` or `EditorialSidebar` does
nothing. Scroll to the top, or pulse through `NavPulse` when already there.

### S11-35 — Exact chart values

`ColumnChart` hides exact values. A hover tooltip on desktop; on touch, press and scrub with a `tickle`
per column.

### S11-36 — Push and pop

Book detail, settings pages, the scanner, Focus Mode and the full-screen cover hard-cut
(`RootScreen`: `EnterTransition.None togetherWith ExitTransition.None`), so the cover morph plays over a
screen that has already cut in. One root push/pop transition from the motion scheme (a fade-through or
short shared axis), with the cover morph running inside it. Update the "currently None" line in the
foundations doc.

### S11-37 — Focus Mode cover

Neither `focusCover` nor `peekBarCover` (`ActiveSessionControllerImpl`) has a key, so the cover jumps on
push. Give `focusCover` the book's cover key and `peekBarCover` its own transition surface.

### S11-38 — Full-screen cover

The full-screen cover (`CoverModelsSnapshot`, `FullScreenCoverShelf`) has no key and reloads behind a
shimmer. Morph the hero cover into the viewer and back.

### S11-39 — Title and rating continuity

Only the cover carries into book detail; the title and rating snap in, and the compact list, which has no
cover, has no continuity at all. Add a shared title and rating key to the card UI model
(`LibraryBookCardMapper`) and match it in `GeneralBookInfoSection`.

### S11-40 — Predictive back

Every pushed screen pops on release, and the library selection and rearrange handlers
(`LibraryEditModeBackHandlers`) use only `onBackCompleted`. Drive the push/pop transition and the cover
morph from the back gesture's progress: the screen shrinks, the previous one shows through, and release
commits or cancels. Android only; the iOS edge swipe gets the plain transition.
