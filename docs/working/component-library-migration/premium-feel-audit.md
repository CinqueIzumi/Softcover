# Premium-feel audit

PF-1 output for [`steps/pf-premium-feel.md`](steps/pf-premium-feel.md). It changes no UI. PF-2 records a verdict
on each row (approve, cut or re-tier). PF-3 moves the approved contract rows into S11, and PF-4 applies the
approved surface rows after S12-4. Delete this file once PF-3 and PF-4 have both landed.

**Columns.** *Today* has been checked against the code. *Touch* names the change, not its API. *Tier*:
**contract** rows change one shared component, modifier, UI model or the app host, and every screen inherits
the change. **Surface** rows polish a single screen. Effort is S, M or L. All motion is gated by
`playDecorativeMotion()`. Haptics are Android and iOS only, because desktop `Haptics` is a no-op. Predictive
back is Android only. A row notes a platform only when it differs further.

Paths: `cc/` = `core/component/.../core/component/`, `orch/` = `orchestration/.../presentation/`, and
`<feature>/` = that module's `presentation/` package in `commonMain` unless a source set is named. Line
numbers will drift.

**Already in place, which these rows build on:** press scale on `BookCard`, `Chip`, `PillButton`,
`MoodTile`, `PreviewTile`, `Toggle`, `SegmentedControl`, `SettingsRow` and FILLED `RhaydusButton`. `NavPulse`
on the Library tab. The gated tab-root swap. `staggeredEntry` and `mutationAnimated` on library grids.
Haptics on long-press select, reorder lift and drop, the pull-to-refresh threshold, `SegmentedControl` and
`StarRatingInput`. Reading's full mark-as-read moment. The cover shared element, `SkeletonCrossfade`, the
`StatNumber` count-up and `RevealedWhenPresent`.

## Contract findings

Ranked by impact over effort. Each approved row becomes an S11 sub-step.

| # | Moment | Today | Touch | Eff | Verdict |
|---|---|---|---|---|---|
| C1 | **Half the app's haptics never fire.** Settings tab reorder, library selection, edition and book reorder, bulk-action sheets, sidebar threshold (`SidebarSection.kt:95`, `ReorderableTabsGroup.kt:46`, `EditionList.kt:106`, `BookList.kt:129`, `LibrarySelectionHeader.kt:20`, `LibrarySheets.kt:27`) | `LocalHaptics` defaults to `NoOpHaptics` (foundation `Haptics.kt:32`), and neither repo ever provides it. Only call sites that use `rememberHaptics()` fire. | Provide the platform haptics as `LocalHaptics` once at the theme root, and use one way of reading them across the app. Every haptic row below depends on this. | S | approve |
| C2 | **Content pops in when loading ends.** All 33 `skeletonMask` sites (`cc/state/SkeletonMask.kt:113-123`) | The shimmer is replaced by content in one frame. Only covers fade, through `SkeletonCrossfade`. | Crossfade from shimmer to content inside the modifier. Keep the grace and minimum-hold timing. | S | approve |
| C3 | **Shared pieces ignore reduced motion.** Cover morph (`cc/cover/Cover.kt:101-113`), `Banner` (`:48-52`), `ProgressBar` (`:24-30`), `WhenReadRow` (`:56-59`), `TopBar` colour (`:47`), `SessionPeekBar` enter/exit (`:70-75`), raw `Modifier.shimmer` on Profile (`ProfileIdentity.kt:61,105`, `GenreRankingSection.kt:64`, `AuthorRepresentationSection.kt:100,113,205`) | None of these goes through `playDecorativeMotion()`, which the foundation makes mandatory. Profile's raw shimmer also skips the grace period, so half the page shimmers early. | Gate each one. Move Profile's shimmer onto `skeletonMask`. Land this before adding any new motion. | S | approve |
| C4 | **Every push and pop hard-cuts.** Book detail, settings pages, scanner, Focus Mode, full-screen cover (`orch/RootScreen.kt:118-122`) | `EnterTransition.None togetherWith ExitTransition.None`. The cover morph plays over a screen that has already cut in. | One root push/pop transition from the motion scheme (a fade-through or short shared axis), with the cover morph running inside it. Update the "currently None" line in the foundations doc. | M | approve |
| C5 | **The back gesture shows no preview.** Every pushed screen, and leaving library selection or rearrange (`MainActivity.kt:33-44`, `LibraryEditModeBackHandlers.kt:19-33`) | Screens pop on release. The edit-mode handlers use only `onBackCompleted`. | Drive C4, including the cover morph, from the back gesture's progress: the screen shrinks, the previous one shows through, and releasing commits or cancels. iOS edge swipe gets C4 only. | L | approve |
| C6 | **Tablet and desktop sheets appear and vanish.** `AdaptiveModalSheet` panel form: share, verdict, lists, re-auth, unknown ISBN (foundation `AdaptiveModalSheet.kt:144-169`) | A bare `Dialog` with no enter or exit animation, and the dismiss is instant. It has a 1 dp outline and no elevation. | Scale and fade in, fade the scrim, and animate the close before `onDismissRequest`, matching the compact sheet. Esc plays the same exit. Add elevation. | M | approve; foundation part → upstream candidates |
| C7 | **The progress sheet vanishes on submit.** Save and "Mark as read" (`reading/action/OnUpdatePageProgressClickAction.kt:61`, Percentage `:33/:90`, Time `:78`; `ReadingOverlays.kt:48,97`) | Submit clears `showProgressSheet`, so the host removes the sheet and the animated `hide()` never runs. This is the app's most-used write. | Submitting runs the animated dismiss before the host clears its state, the way `dismissesSheet` footer buttons already do. Check the other hosts for the same pattern. | M | approve |
| C8 | **The book detail hero cover jumps** on load and on an edition change (`cc/cover/Cover.kt:137-185`, `CoverModelsSnapshot.kt:36-57`) | The new source gets a new placeholder key, so the old image is dropped and a shimmer flashes. The loading slot uses `fillMaxSize`/`Fit`, while the loaded slot sizes to the cover's ratio with a shadow. | `Cover` keeps the last image through a source swap and crossfades to the new one. Loading and loaded slots share one size and treatment. | M | approve |
| C9 | **Library cold start animates placeholders.** (`BookList.kt:74,83,116-118`, `LibraryStandIns.kt:23-24`, `EditionList.kt:61,71,91-93`) | Placeholders keyed `Book(-1..-N)` become the mutation animator's baseline. Every real card then counts as inserted and pulses at once, the welcome stagger plays on the placeholders, and `SkeletonCrossfade` never runs. | Placeholders never become the baseline. The hand-over is one quiet crossfade, and the stagger plays on the real books. | M | approve |
| C10 | **Each keystroke throws search results away.** Typing and sort (`explore/action/OnQueryChangeAction.kt:33-42`, `BookCardModelsCollector.kt:87-88`, `OnSortModeChangeAction.kt:42`) | `isLoading` replaces the list with stand-ins. After the 1 s debounce the real rows snap back in. | While a refresh runs, keep the old results (dimmed, or with the field spinner only) and crossfade to the new ones. Show stand-ins only for the first query. | M | approve |
| C11 | **Numbers snap while their bars glide.** Reading hero % and page (`FeaturedProgressStat.kt:45-59`), book detail % (`InProgressSection.kt:49-53`, `ShelveControlCard.kt:328-336`), streak (`StreakStrip.kt:84-91`), "N selected" / "Show N titles" / "N titles" (`SelectionHeader.kt:55-62`, `LibraryDisplayDerivation.kt:79`, `LibraryStats.kt:15`) | Plain text, which `StatNumber`'s KDoc calls a bug for a number that changes. | Map each to `StatNumberUiModel` and render it with `StatNumber`. | S | approve |
| C12 | **The count-up plays behind the shimmer.** Profile hero totals (`cc/statistic/StatNumber.kt:58-88`, `ReadingAtlasMapper.kt:12-48`) | The roll starts during the skeleton's hold. The tick draws while masked, and on large values it becomes a solid line. | Start the roll when the mask clears. Keep the tick a tick on large ranges. | S | approve |
| C13 | **Charts jump to size.** Columns, genre bars and proportions on load, Books/Pages and "hide untagged" (`cc/chart/ColumnChart.kt:57-92`, `RankedBars.kt:56`, `ProportionChart.kt:41-47`) | Sizes are plain values. The 8 loading columns are swapped out all at once, while the control above animates. | Bars grow from the baseline, morph between metrics and slide to new proportions. Labels and caption crossfade with them. | M | approve |
| C14 | **Selecting a cover snaps.** (`cc/bookcard/BookCardParts.kt:57-93`, `cc/selection/SelectionMark.kt:31-64`) | The dim, ring, tint and mark switch instantly. Entering selection dims every cover in one frame. | Animate the dim, ring and fill. The check pops in. | S | approve |
| C15 | **Dragged items don't lift.** Library covers and settings tabs (`BookList.kt:223-249`, `EditionList.kt:213-250`, `ReorderableTabsGroup.kt:66-121`, `ReorderableRow.kt:58-71`) | Covers ignore `isDragging`, and tabs only change tint. There is no scale, no shadow and no tick per slot. | One reorder treatment: scale and shadow while dragged, settling on drop, and a `tickle` per slot crossed. | S | approve |
| C16 | **Selectable controls give no tick.** Chips and `SheetRow` (`cc/chip/Chip.kt:98,183`, `SheetRow.kt:42-55`), `ToggleRow` (`:57-66,100-104`), `PreviewTile` (`:58-101`), settings check rows (`SettingsSectionShared.kt:35-56`) | Only `SegmentedControl` fires `select`. The `ToggleRow` switch thumb never grows on press because the row doesn't share its interaction source. | Fire `select` on every selection change. Share the row's interaction source with the `Switch`. | S | approve |
| C17 | **Selection states snap.** `SheetRow` ink, `PreviewTile` ring, settings check, `Toggle` glyph (`cc/control/Toggle.kt:69-72`) | Colours and glyphs swap in one frame. `Chip` hand-writes `tween(220)`. | Animate these with motion-scheme specs. The `Toggle` glyph crossfades and pops when it fills. | S | approve |
| C18 | **Committing a sheet is silent, and its spinner lands on the wrong button.** `SheetScaffold` footer (`cc/sheet/SheetScaffold.kt:309,322-337`) | Saving a rating gives no haptic. A secondary button's busy state spins the primary. | A primary button that commits data fires one `commit`. The spinner goes on the button that is running and fades in. | S | approve |
| C19 | **Field errors don't land on the field.** Rejected API key (`OnApiKeySaveClickAction.kt:16-27`, `OnboardingShelf.kt:132-184`), list name taken (`CreateListSheetContent.kt:86-97`), page past the end (`ProgressBottomSheetTabs.kt:184-239`, `OnPageDraftChangedAction.kt:63`), invalid ISBN (`BarcodeScannerShelf.kt:74-97`) | `TextFieldUiModel` has no error hook, so there is only a snackbar. The page clamp silently turns 9999 into 320. `shakeOnError` is used only by `BookCard`. | Add an error-and-shake trigger to the field model, with an outline. The shake brings `reject` with it. | S | approve |
| C20 | **Steppers are flat.** (`cc/progress/StepperCircle.kt:32-50`) | Ripple only, with no hold. Going from page 280 to 312 takes 32 taps. | Press scale, a `tickle` per step, and long-press auto-repeat that speeds up. | S–M | approve |
| C21 | **Progress-sheet tabs jump.** (`cc/progress/UpdateProgressBottomSheet.kt:106-123`) | A plain `when`, so the sheet height jumps. | Crossfade the tab body with a size transform. | S | approve |
| C22 | **The theme changes in one frame.** (`core/designsystem/.../theme/Theme.kt:138-158`, `orch/App.kt:114-122`) | The new `colorScheme` repaints the whole app at once. | Animate each colour role (~350 ms) in the theme host. Flip the system bars at the midpoint. | M | cut |
| C23 | **Add-to-list is flat.** (`cc/lists/ChooseListsBottomSheet.kt:140-260`) | The bookmark swaps instantly. The spinner swaps through `alpha(0/1)`. No haptic. | The bookmark fills with a pop, the chip and spinner crossfade, and `select` fires. | S | approve |
| C24 | **Search chrome lags the rest.** (`cc/topbar/SearchTopBar.kt:77,140-180,284-290`, `cc/header/MastheadRefreshAction.kt:17-23`) | The barcode button ripples. The border and the spinner/× slot pop. Nothing separates the bar from scrolled content. The desktop Refresh text swaps in one frame. | Press scale on the barcode button. Animate the border and crossfade the slot. Lift the bar when content scrolls under it. Crossfade the Refresh text. | S | approve |
| C25 | **Desktop surfaces don't react to hover.** Cover cards (`cc/bookcard/BookCard.kt:90-94`), chips, `PillButton`, tiles, `SheetRow`, shelf sidebar (`cc/sidebar/SidebarSection.kt:107-138`) | Cover cards hard-code `hoverHighlight = false`. Primitives show a hand cursor and nothing else. The sidebar strips the ripple without adding press scale, which breaks a foundation rule. | Pair `hoverHighlight` with `pointerHandCursor` in every clickable primitive, and let cover cards lift the cover. The sidebar gets press scale and an animated selection. | M | approve |
| C26 | **Snackbars cover the bottom bar on phones.** (`orch/App.kt:160-168`) | The host sits above only the nav-bar inset. | Lift the host above the measured bottom chrome. | S–M | approve; bug, fix now |
| C27 | **Desktop share and save mislead.** (`cc/share/ShareCardSheetContent.kt:86-122`, foundation `JvmShareCardCapture.kt`) | Share copies to the clipboard without saying so. Cancelling the save dialog reports a failure. The labels say "gallery". | Confirm "Copied to clipboard", say nothing when a save is cancelled, and use desktop labels. | S–M | approve; foundation part → upstream candidates |
| C28 | **Share-card success is silent.** (`cc/share/ShareCardSheetContent.kt:96-122`) | A snackbar on save, or the sheet just closes. | `commit` and a brief settle on the preview. On failure, `reject` and a shake. | S | approve |
| C29 | **The update callout snaps between states.** (`cc/callout/Callout.kt:167-195`) | The call-to-action swaps in one frame. | Crossfade it. `commit` on Downloaded, `reject` on Failed. | S | approve |
| C30 | **Tab switches use their own timing.** (`orch/BottomBarScreen.kt:58,311-316`) | A hand-written `tween(200)`. | Take the specs from `MaterialTheme.motionScheme`. | S | approve |
| C31 | **The Focus Mode cover doesn't travel.** (`ActiveSessionControllerImpl.kt:51-52`, `FocusModeShelf.kt:94`) | Neither `focusCover` nor `peekBarCover` has a key, so the cover jumps on push. | Give `focusCover` the book's cover key. Give `peekBarCover` its own transition surface. | S | approve |
| C32 | **The full-screen cover doesn't grow out of the hero.** (`CoverModelsSnapshot.kt:69-74`, `FullScreenCoverShelf.kt:129-141`) | It has no key and reloads behind a shimmer. | Morph the hero cover into the viewer and back. | M | approve |
| C33 | **Only the cover carries into book detail.** (`cc/cover/Cover.kt:98-114`, `GeneralBookInfoSection.kt:193-301`, `LibraryBookCardMapper.kt:66-96`) | The title and rating snap in. The compact list has no cover, so it has no continuity at all. | A shared title and rating key on the card UI model. | L | approve |
| C34 | **`commit` is spent on routine actions.** Library moves, Remove and list toggles (`LibrarySelectionHeader.kt:31`, `LibrarySheets.kt:56,79`), Focus Mode saves (`FocusModeScreen.kt:42`). `threshold` fires on session start and stop. | It fires before the outcome is known, and failures never get `reject`. | Keep `commit` for finishing a book, creating a list and a session recap. Use `select` for routine changes and nothing on Remove. Fire after the outcome, and fire `reject` on failure. | S | approve |
| C35 | **Explore pull-to-refresh is the odd one out.** (`explore/screen/section/EditorialContent.kt:40-55`) | A Material `IndicatorBox` with no tick. | Move Explore onto `PullToRefreshEyebrow`. | S | approve |

## Surface findings

Grouped by screen and ranked within each group. These rows are PF-4's work list.

| # | Screen | Moment | Today | Touch | Eff | Verdict |
|---|---|---|---|---|---|---|
| S1 | Library | **Bulk Mark as Read gets no celebration.** (`SelectionHeader.kt:107-111`, `BookList.kt:300-311`) | The books fade out. There is no burst. | Play `MarkAsReadBurst` when the batch succeeds. | S | approve |
| S2 | Library | **The shelf cuts to the empty state.** (`BookList.kt:60-72`, `EditionList.kt`) | An early `return` skips the removal fade. | Let the last book fade out, then crossfade to the empty state. | S | approve |
| S3 | Library | **Entering selection makes the grid jump.** (`LibraryScreenLayout.mobile.kt:158-232`, `.jvm.kt:109-145`) | The whole header swaps at once. | Crossfade the headers and animate the height change. | S | approve |
| S4 | Library | **The shelf title lags the pager.** (`MastheadHeader.kt:104-114`) | It swaps at the swipe's midpoint. | Slide or fade the title with the pager. `select` when it settles. | S | approve |
| S5 | Library | **Switching shelves on desktop cuts.** (`LibraryScreenLayout.jvm.kt:159-176`) | `when (currentTab)` | A short crossfade. | S | approve |
| S6 | Library | **Control-line affordances pop in.** (`LibraryControlLine.kt:63-148`, `BookList.kt:355-359`) | The arrow swaps icon. The chip, pill and drag handles appear via `if`. | Rotate the arrow and expand or fade the rest in. | S | approve |
| S7 | Library | **Back out of selection shows no preview.** (`LibraryEditModeBackHandlers.kt:19-33`) | The exit is instant. | Scrub the exit with the back gesture's progress, on top of C5. | M | approve |
| S8 | Reading | **Finishing from Focus Mode gets no celebration.** (`ActiveSessionControllerImpl.kt:112-122`, `OnPageFocusLostAction.kt:34`) | The outcome is ignored and `commit` fires before the save. No burst, no verdict. A failed save is silent. | On finish, play the burst and open the verdict sheet. Haptic after success, `reject` on failure. | M | approve |
| S9 | Reading | **The next book cuts into the hero.** (`ReadingBooksColumn.kt:60`, `MarkAsReadController.kt:94-116`) | The key changes, so the new book appears in one frame. | It rises into the hero, and its cover travels up from its row. | M | approve |
| S10 | Reading, Profile | **Streak milestones pass unmarked.** (`StreakStrip.kt:44-66`, `ReadingAtlasMapper.kt:40-48`, `StatTile.kt:144-187`) | Today's dot snaps on. Day 30 looks like day 2. `milestone()` is never used for streaks. | Animate the dot. At 7, 30, 100 and 365 days, fire `milestone` once with a small burst. | M | approve |
| S11 | Reading | **The peek bar is flat.** (`SessionPeekBar.kt:70-155`) | No press scale or hover. Play and pause swap in one frame. | Press scale and hover. Crossfade the glyph and the elapsed line. | S | approve |
| S12 | Focus Mode | **Paused looks broken.** (`FocusModeShelf.kt:125-179`) | The colours and button snap. | Animate the timer colour, breathe slowly while paused, and crossfade the button. | S | approve |
| S13 | Reading | **The session button swaps abruptly.** (`FeaturedSessionButton.kt:51-111`) | `when` | Crossfade and resize it in step with the peek bar. | S | approve |
| S14 | Reading | **The empty and populated states cut.** (`ReadingScreenLayout.mobile.kt:100-131`, `.jvm.kt:37-71`) | The burst plays over a hard cut. | Crossfade the two bodies. | S | approve |
| S15 | Reading | **The Trending tile has no press feedback.** (`TrendingTileSection.kt:19-26`) | It shows a hand cursor and nothing else. | Press scale and hover. | S | approve |
| S16 | Reading | **Desktop rows jump up.** (`ReadingScreenLayout.jvm.kt:121-138`) | A non-lazy `Column`. | Collapse the removed row smoothly. | M | approve |
| S17 | Reading | **The hero has no depth.** (`FeaturedBackdropCard.kt:103-115`) | It scrolls rigidly. | Parallax on the backdrop. Mobile only. | M | approve |
| S18 | Book detail | **The burst plays under the verdict sheet.** (`BookDetailScreen.kt:91-97`) | Both are triggered in the same frame. | Play the burst first, then open the sheet. | S | approve |
| S19 | Book detail | **Shelving never pulses the Library tab.** (`ShelveControlCard.kt:111-152`) | Only Reading pulses it. | Pulse the tab when a shelve is applied. | S | approve |
| S20 | Book detail | **A status change rebuilds the lens in one frame.** (`YoursLensContent.kt:33-65`) | `when (status)`, and `VerdictBlock` pops in. | A size-aware crossfade and a vertical reveal. | S | approve |
| S21 | Book detail | **Reviews snap open.** (`ReviewsSection.kt:155-183`) | `maxLines` flips. | Animate the height and fade the spoiler in. | S | approve |
| S22 | Book detail | **The top-bar title pops in, and its scroll behaviour does nothing.** (`BookDetailScreenLayout.mobile.kt:50-104`) | Index threshold. The list has no `nestedScroll`. | Drive the title from scroll progress. Wire `nestedScroll` or drop the scroll behaviour. | M | approve; wire `nestedScroll` |
| S23 | Book detail | **The hero scrolls like any other row.** (`GeneralBookInfoSection.kt:76-103`) | It moves 1:1 with the list. | Parallax on the backdrop. Android below 12 can't blur, so it needs a heavier dim. | M | approve |
| S24 | Book detail | **The full-screen cover jumps to 2.5×.** (`FullScreenCoverShelf.kt:49-56`, `.mobile.kt:23-35`) | No animation. No swipe to dismiss. | Animated zoom, swipe-down to dismiss, and soft edges. | M | approve |
| S25 | Book detail | **The wide pane cuts between books.** (`BookDetailPaneHost.kt:33-47`, `BottomBarScreen.kt:163-184`) | `key(destination.id)` | A fade-through. | S | approve |
| S26 | Book detail | **Shelve rows have no desktop affordances.** (`ShelveControlCard.kt:275-295`) | No hand cursor or hover. | Add both. | S | approve |
| S27 | Explore | **A search with no results shows a blank list.** (`ActiveSearchContent.kt:64-75`, `DesktopSearchResults.kt:74-85`) | No empty state. | `EmptyState` with ways forward. | S | approve; bug, fix now |
| S28 | Explore | **Feed, focus and results cut.** (`ExploreScreenLayout.mobile.kt:120-140`, `.jvm.kt:114-126`) | `when (searchPhase)` | A fade-through, with the results settling upward. | S | approve |
| S29 | Explore | **Dismissing an Up-next card makes the rail jump.** (`ExploreCardRail.kt:40`, desktop `:42`) | Undo jumps the same way. | `mutationAnimated` on the rail. | S | approve |
| S30 | Explore | **The featured "Want to read" flips flatly.** (`FeaturedCard.kt:135-148`) | No transition or haptic. | Morph the label and fire `commit`. | S | approve with `select`, not `commit` |
| S31 | Explore | **Recent and hidden rows snap away.** (`SearchFocusContent.kt:74-87`, `HiddenSuggestionsShelf.kt:107-114`) | `forEach` removal. | Fade and collapse the rows, then crossfade to the empty state. | S | approve |
| S32 | Explore | **Search focus and dismiss rows ripple.** (`SearchFocusContent.kt:128,159`, `ContinueSeriesMenuSheet.kt:76`) | Plain `.clickable`. | A small press response. | S | approve |
| S33 | Explore | **The result bookmark doesn't fill.** (`BookCardControls.kt:42-45`) | It changes colour only. | The C17 glyph pop, plus `commit` when adding. | S | approve with `select`, not `commit` |
| S34 | Profile | **The page lands all at once.** (`ProfileReadingLife.kt:24-96`, `ReadingAtlasSection.kt:35-59`) | No stagger. | A one-time `StaggeredEntry` welcome. | S | approve |
| S35 | Profile | **"Share your reading year" does nothing while loading.** (`ShareEntryRow.kt:48-53`) | It press-scales, then nothing happens. | Make it inert until the card exists, like the top-bar button. | S | approve; bug, fix now |
| S36 | Profile | **The bar never shows whose profile it is.** (`ProfileScreenLayout.mobile.kt:51-66`, `.jvm.kt:85-101`) | No scroll behaviour. | Lift the bar on scroll and fade the name into it. | M | approve |
| S37 | Onboarding | **Sign-in cuts straight to the app.** (`OnboardingScreen.kt:42-44`, `LoadingSheet.kt:42-47`, `orch/App.kt:137-145`) | `key(authenticated)` swaps while the sheet is still open. Desktop shows a blank frame. | `commit`, then a crossfade. A wordmark on desktop's first frame. | M | approve |
| S38 | Settings | **A dropped tab jumps into its slot.** (`ReorderableTabsGroup.kt:148-180`) | The offset resets to 0. | Spring the tab into its slot, and fire `drop` when it lands. | S | approve |
| S39 | Settings | **Hiding a tab snaps.** (`ReorderableRow.kt:55,72,199`) | The alpha jumps and the label swaps. | Animate the alpha and crossfade the label. | S | approve |
| S40 | Settings | **Saving tabs isn't confirmed.** (`LibraryVisibilitySaveBar.kt:29-51`) | The button just disables. | Show "Saved" briefly with `commit`. | S | approve with `select`, not `commit` |
| S41 | Settings | **The update callout pops in.** (`SettingsScreenLayout.mobile.kt:101-114`, `AboutPane.kt:25`) | `?.let` | Expand and fade it in. | S | approve |
| S42 | Onboarding | **Paste from clipboard ripples.** (`OnboardingShelf.kt:118-124,188-200`) | Plain `clickable`. | Press scale. A paste fires `select` and highlights the field. | S | approve |
| S43 | Settings | **Sub-pages scroll under a flat bar.** (`AppearanceSettingsScreenLayout.mobile.kt:27-52`, `LibraryVisibilitySettingsScreenLayout.mobile.kt:30-60`) | No `scrollBehavior`. | A pinned scroll behaviour. | S | approve |
| S44 | Onboarding | **The page number lags the swipe.** (`OnboardingScreenLayout.mobile.kt:163-190`) | It snaps when the page settles. | Drive the tint from the pager offset. | S | approve |
| S45 | Settings | **Desktop categories cut.** (`SettingsScreenLayout.jvm.kt:74`) | `when (selected)` | A fade-through. | S | approve |
| S46 | Scanner | **A successful scan isn't marked.** (`BarcodeScannerScreen.kt:64-81`, `.mobile.kt:91-97`) | The camera is replaced by a grey spinner. No haptic. | `threshold`, the reticle tightens, the frame is held under a scrim, then the C4 push. | M | approve |
| S47 | Scanner | **The scanner doesn't look like it's searching.** (`BarcodeScanner.android.kt:96-146`, `.ios.kt`) | A static rectangle, dimmed only at the top. | A cut-out scrim and a slow pulse on the reticle. | S–M | approve |
| S48 | Scanner | **Switching to manual entry cuts.** (`BarcodeScannerScreenLayout.mobile.kt:52-78`) | Hard swaps. | Crossfade. | S | approve |
| S49 | Share | **Switching card variants swaps hard.** (`ShareCardSheetContent.kt:142`, `ShareBookBottomSheet.kt:22-37`) | The sheet height jumps. | Crossfade and animate the size. | S | approve |
| S50 | Share | **The preview looks flat.** (`ShareCard.kt:97-100`, `ScaledShareCardPreview.kt`) | No shadow. | A shadow on the preview host only, never on the captured card. | S | approve |
| S51 | Desktop nav | **The sidebar selection snaps.** (`EditorialSidebar.kt:99-125`) | No hover or hand cursor. | Animate the selection, and add hover and a hand cursor. | S | approve |

## Open suggestions

Missing states, affordances and rough edges beyond the audit categories. Rows marked **bug** are defects
found during the sweep.

| # | Tier | Suggestion | Touch | Eff | Verdict |
|---|---|---|---|---|---|
| O1 | contract | **Re-tapping the current tab does nothing.** (`BottomNavigationBar.kt:37,66`, `NavigationRailBar.kt:26`, `EditorialSidebar.kt:85`) | Scroll to the top, or pulse through `NavPulse` if already there. | M | approve |
| O2 | contract | **Removing books can't be undone.** Bulk remove (`OnBulkRemoveFromLibraryAction.kt`), and book detail's Remove, which deletes status, progress and verdict with no confirmation (`OnRemoveBookClickAction.kt:19-33`) | A snackbar with Undo. | M | approve; bug, fix now |
| O3 | contract | **Exact chart values are hidden.** (`ColumnChart.kt:47`) | A hover tooltip on desktop. On touch, press and scrub with a `tickle` per column. | M | approve |
| O4 | contract | **The update download has no progress.** (`AppUpdateDataSourceImpl.kt:8-17`, `AppUpdateCalloutMapper.kt:27-31`) | Carry bytes downloaded and show a determinate bar. Android and desktop. | M | approve |
| O5 | contract | **No blur behind sheets.** | Blur or deepen the dim where the platform supports it, after C6. | L | cut |
| O6 | surface | **bug: Back on onboarding pages 2–3 closes the app.** (`OnboardingScreenLayout.mobile.kt:70-122`) | Back goes to the previous page. | S | approve; fix now |
| O7 | surface | **bug: Library tabs discard unsaved changes on back.** (`LibraryVisibilitySettingsScreen.kt:30`) | A Save / Discard sheet, or autosave. | S | approve; fix now |
| O8 | surface | **bug: A failed tabs save looks saved.** (`OnSaveLibraryVisibilityAction.kt:23-38`) | Show the failure and keep the draft. | S | approve; fix now |
| O9 | surface | **bug: A failed log-out is silent.** (`OnLogOutClickAction.kt`) | A snackbar, or keep the sheet busy. | S | approve; fix now |
| O10 | surface | **bug: Enter / Done doesn't submit the API key.** (`OnboardingShelf.kt:136-139`) | Route submit to the save action. | S | approve; fix now |
| O11 | surface | **bug: Save works on an empty key, and an empty paste clears the field.** (`OnboardingUiState.kt:21`, `OnboardingShelf.kt:118-124`) | Disable Save when the key is blank, and ignore an empty paste. | S | approve; fix now |
| O12 | surface | **bug: "1 days" and "across 1 books".** (`ReadingAtlasMapper.kt:47`, `RatingsHistogramSection.kt:125`) | Pluralise. | S | approve; fix now |
| O13 | surface | **bug: Focus Mode pops during composition** when the session ends from the notification (`FocusModeScreen.kt:46-52`) | Pop from an effect so the exit animation plays. | S | approve; fix now |
| O14 | surface | **bug?: System back with a pane book open** likely falls through the root navigator (`BookDetailPaneHost.kt`). Check on a tablet. | Back closes the pane. Esc does the same on desktop. | S | approve; device check, then fix now |
| O15 | surface | **Offline banner content may jump** (`orch/RootScreen.kt:80-92`). Check on a device. | Tie the inset hand-off to the banner's animated height. | S | approve; device check, then fix now |
| O16 | surface | **Ending a session says nothing.** (`FocusModeShelf.kt:189-193`) | A recap ("42 min · 18 pages") with a quick page update. | M | approve |
| O17 | surface | **Focus Mode has no steppers.** (`FocusModeShelf.kt:225-236`) | Reuse the C20 stepper row, and show pages read this session. | S–M | approve |
| O18 | surface | **"Also reading" rows have no quick actions.** (`ReadingCardEventHandler.kt:27-30`) | A long-press or right-click menu: start session, mark as read, update progress. | M | approve |
| O19 | surface | **Bulk actions pause silently.** (`OnBulkMoveShelfAction.kt:37-56`) | Show "3 of 12" in the selection header. | S | approve |
| O20 | surface | **Creating a list gives no feedback.** (`CreateListScreen.kt:38-53`) | Progress in the button and `commit` on success. | S | approve |
| O21 | surface | **Book detail and Profile have no pull-to-refresh.** | `PullToRefreshEyebrow`. Mobile. | M | approve |
| O22 | surface | **A saved rating doesn't visibly land.** (`cc/verdict/VerdictBlock.kt:103-120`) | A one-time star sweep. | M | approve |
| O23 | surface | **Hero metadata snaps on an edition change.** (`GeneralBookInfoSection.kt:212-341`) | Crossfade it. | S | approve |
| O24 | surface | **The desktop detail sidebar has no scrollbar.** (`BookDetailScreenLayout.jvm.kt:141-146`) | `DesktopVerticalScrollbar`. | S | approve |
| O25 | surface | **Long descriptions are a wall of text.** (`AboutSection.kt:61-76`) | Collapse behind "Show more". Check against the preserve-existing-UI rule first. | S | approve |
| O26 | surface | **Rails stop mid-card.** (`ExploreCardRail.kt:35-39`) | Snap to whole cards. | S | approve |
| O27 | surface | **Mood results lose the mood's colour.** (`OnMoodChipClickAction.kt:44`) | Carry the mood's colour into the results. | M | approve |
| O28 | surface | **Load-more rows pop in.** (`SearchResultsSection.kt:117-126`) | Fade in the new rows. | S | approve |
| O29 | surface | **The floating bar never hides.** (`BottomNavigationBar.kt:56-58`) | Tuck it away on scroll. | M | cut |
| O30 | surface | **Desktop has no keyboard or mouse-back navigation.** (`desktopApp/.../Main.kt`) | Tab shortcuts, back shortcuts and a minimal MenuBar. | M | approve |
| O31 | surface | **The scanner has no torch.** (`BarcodeScanner.android.kt:50-76`) | A torch toggle. | S–M | approve |
| O32 | surface | **Changing the library layout cuts.** (`LibrarySectionShared.kt:113-153`) | Morph the covers between layouts. | L | cut |
| O33 | surface | **The shelves chevron stays still.** (`MastheadHeader.kt:118-130`) | Rotate it while the sheet is open. | S | approve |
| O34 | surface | **A progress update gives no cue where it landed.** (`ReadingOverlays.kt:57-84`) | Briefly highlight the updated row. | S | approve |
| O35 | surface | **iOS has no lock-screen session.** (`PlatformSessionModule.ios.kt`) | A Live Activity timer. | L | cut |
| O36 | surface | **No year-in-review moment.** (`ShareEntryRow.kt:83-94`) | A richer entry at the end of the year. | M | cut |
| O37 | surface | **A zero streak looks live.** (`ReadingAtlasMapper.kt:40-48`) | A muted "Start today" state. | S | approve |
| O38 | surface | **The share row and sheet use different names.** (`ProfileSheetScaffoldBuilder.kt`, `ShareEntryRow.kt:83,91`) | One name. PF-5 may take this. | S | approve; PF-5 takes it |
| O39 | surface | **First-login progress stalls at 20 %.** (`OnApiKeySaveClickAction.kt:47-63`) | Report real stages. | M | approve |
| O40 | surface | **The onboarding hero appears all at once.** (`OnboardingScreenLayout.mobile.kt:192-255`) | A one-time stagger. | S | approve |
