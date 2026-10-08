# Premium-feel audit

PF-1 output for [`steps/pf-premium-feel.md`](steps/pf-premium-feel.md), with the PF-2 verdict on each row.
The approved contract rows are S11-5 to S11-40 in
[`steps/s11-contract-retrofit.md`](steps/s11-contract-retrofit.md) and SL-15a–e in
[`steps/sl-lean-loading.md`](steps/sl-lean-loading.md); a surface row citing one names that
sub-step. PF-4 applies the approved surface rows after S12-4, then deletes this file.

**Columns.** *Today* has been checked against the code. *Touch* names the change, not its API. Each row
polishes a single screen. Effort is S, M or L. All motion is gated by
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
| S7 | Library | **Back out of selection shows no preview.** (`LibraryEditModeBackHandlers.kt:19-33`) | The exit is instant. | Scrub the exit with the back gesture's progress, on top of S11-40. | M | approve |
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
| S27 | Explore | **A search with no results shows a blank list.** (`ActiveSearchContent.kt:64-75`, `DesktopSearchResults.kt:74-85`) | No empty state. | `EmptyState` with ways forward. | S | fixed |
| S28 | Explore | **Feed, focus and results cut.** (`ExploreScreenLayout.mobile.kt:120-140`, `.jvm.kt:114-126`) | `when (searchPhase)` | A fade-through, with the results settling upward. | S | approve |
| S29 | Explore | **Dismissing an Up-next card makes the rail jump.** (`ExploreCardRail.kt:40`, desktop `:42`) | Undo jumps the same way. | `mutationAnimated` on the rail. | S | approve |
| S30 | Explore | **The featured "Want to read" flips flatly.** (`FeaturedCard.kt:135-148`) | No transition or haptic. | Morph the label and fire `commit`. | S | approve with `select`, not `commit` |
| S31 | Explore | **Recent and hidden rows snap away.** (`SearchFocusContent.kt:74-87`, `HiddenSuggestionsShelf.kt:107-114`) | `forEach` removal. | Fade and collapse the rows, then crossfade to the empty state. | S | approve |
| S32 | Explore | **Search focus and dismiss rows ripple.** (`SearchFocusContent.kt:128,159`, `ContinueSeriesMenuSheet.kt:76`) | Plain `.clickable`. | A small press response. | S | approve |
| S33 | Explore | **The result bookmark doesn't fill.** (`BookCardControls.kt:42-45`) | It changes colour only. | The S11-17 glyph pop, plus `commit` when adding. | S | approve with `select`, not `commit` |
| S34 | Profile | **The page lands all at once.** (`ProfileReadingLife.kt:24-96`, `ReadingAtlasSection.kt:35-59`) | No stagger. | A one-time `StaggeredEntry` welcome. | S | approve |
| S35 | Profile | **"Share your reading year" does nothing while loading.** (`ShareEntryRow.kt:48-53`) | It press-scales, then nothing happens. | Make it inert until the card exists, like the top-bar button. | S | fixed |
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
| S46 | Scanner | **A successful scan isn't marked.** (`BarcodeScannerScreen.kt:64-81`, `.mobile.kt:91-97`) | The camera is replaced by a grey spinner. No haptic. | `threshold`, the reticle tightens, the frame is held under a scrim, then the S11-36 push. | M | approve |
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
| O6 | surface | **bug: Back on onboarding pages 2–3 closes the app.** (`OnboardingScreenLayout.mobile.kt:70-122`) | Back goes to the previous page. | S | fixed |
| O7 | surface | **bug: Library tabs discard unsaved changes on back.** (`LibraryVisibilitySettingsScreen.kt:30`) | A Save / Discard sheet, or autosave. | S | fixed |
| O8 | surface | **bug: A failed tabs save looks saved.** (`OnSaveLibraryVisibilityAction.kt:23-38`) | Show the failure and keep the draft. | S | fixed |
| O9 | surface | **bug: A failed log-out is silent.** (`OnLogOutClickAction.kt`) | A snackbar, or keep the sheet busy. | S | fixed |
| O10 | surface | **bug: Enter / Done doesn't submit the API key.** (`OnboardingShelf.kt:136-139`) | Route submit to the save action. | S | fixed |
| O11 | surface | **bug: Save works on an empty key, and an empty paste clears the field.** (`OnboardingUiState.kt:21`, `OnboardingShelf.kt:118-124`) | Disable Save when the key is blank, and ignore an empty paste. | S | fixed |
| O12 | surface | **bug: "1 days" and "across 1 books".** (`ReadingAtlasMapper.kt:47`, `RatingsHistogramSection.kt:125`) | Pluralise. | S | fixed |
| O13 | surface | **bug: Focus Mode pops during composition** when the session ends from the notification (`FocusModeScreen.kt:46-52`) | Pop from an effect so the exit animation plays. | S | fixed |
| O14 | surface | **bug: System back with a pane book open closes the app** (seen on an emulator tablet in landscape). The pane's nested `Navigator` holds one screen, so back falls through to the root (`BookDetailPaneHost.kt`, `BottomBarScreen.kt:169`). | Back closes the pane: a `NavigationBackHandler` (as in `LibraryEditModeBackHandlers.kt`) enabled while `paneBook` is set, completing to `onClose`. Screens or sheets pushed on the overlay navigator still take back first. Esc does the same on desktop (Reading tab). | S | fixed |
| O15 | surface | **bug: Offline banner content jumps for one frame**, both ways, by the status-bar height (seen on an emulator at 10× animator scale). `orch/RootScreen.kt:88-89` consumes `statusBars` as soon as `visible` flips, while the banner's `AnimatedVisibility` height starts from 0. | Consume only as much of the status-bar inset as the banner covers right now (its animated height, capped at the inset), resolved in layout, not through a state round-trip that lags a frame. Repro: `adb shell settings put global animator_duration_scale 10`, `adb shell cmd connectivity airplane-mode enable`/`disable`, burst `screencap`. | S | fixed |
| O16 | surface | **Ending a session says nothing.** (`FocusModeShelf.kt:189-193`) | A recap ("42 min · 18 pages") with a quick page update. | M | approve |
| O17 | surface | **Focus Mode has no steppers.** (`FocusModeShelf.kt:225-236`) | Reuse the S11-23 stepper row, and show pages read this session. | S–M | approve |
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
