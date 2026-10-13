# Premium-feel findings

The approved surface rows of the premium-feel audit, the work list for [`README.md`](README.md). Rows that were
fixed, cut or moved to the copy audit are gone.

> [!WARNING]
> **Stale and not approved for building.** Re-audit and walk every row with the user first; see the
> warning in [`README.md`](README.md).

**Columns.** *Today* was checked against the code when the audit was written. *Touch* names the change, not its
API. Effort is S, M or L. A row notes a platform only when it differs from the README's decisions.

Paths: `cc/` = `core/component/.../core/component/`, `orch/` = `orchestration/.../presentation/`, and
`<feature>/` = that module's `presentation/` package in `commonMain` unless a source set is named. Line
numbers will drift.

**Already in place, which these rows build on:** press scale on `BookCard`, `Chip`, `PillButton`,
`MoodTile`, `PreviewTile`, `Toggle`, `SegmentedControl`, `SettingsRow` and FILLED `RhaydusButton`. `NavPulse`
on the Library tab. The gated tab-root swap. `staggeredEntry` and `mutationAnimated` on library grids.
Haptics on long-press select, reorder lift and drop, the pull-to-refresh threshold, `SegmentedControl` and
`StarRatingInput`. Reading's full mark-as-read moment. The cover shared element, `SkeletonCrossfade`, the
`StatNumber` count-up and `RevealedWhenPresent`. Animated selection states, the `StepperCircle` press and
auto-repeat, and the one root push/pop transition driven by predictive back.

## Surface findings

| # | Screen | Moment | Today | Touch | Eff |
|---|---|---|---|---|---|
| S1 | Library | **Bulk Mark as Read gets no celebration.** (`SelectionHeader.kt:107-111`, `BookList.kt:300-311`) | The books fade out. There is no burst. | Play `MarkAsReadBurst` when the batch succeeds. | S |
| S2 | Library | **The shelf cuts to the empty state.** (`BookList.kt:60-72`, `EditionList.kt`) | An early `return` skips the removal fade. | Let the last book fade out, then crossfade to the empty state. | S |
| S3 | Library | **Entering selection makes the grid jump.** (`LibraryScreenLayout.mobile.kt:158-232`, `.jvm.kt:109-145`) | The whole header swaps at once. | Crossfade the headers and animate the height change. | S |
| S5 | Library | **Switching shelves on desktop cuts.** (`LibraryScreenLayout.jvm.kt:159-176`) | `when (currentTab)` | A short crossfade. | S |
| S7 | Library | **Back out of selection shows no preview.** (`LibraryEditModeBackHandlers.kt:19-33`) | The exit is instant. | Scrub the exit with the back gesture's progress, on top of the predictive-back push/pop. | M |
| S8 | Reading | **Finishing from Focus Mode gets no celebration.** (`ActiveSessionControllerImpl.kt:112-122`, `OnPageFocusLostAction.kt:34`) | The outcome is ignored and `commit` fires before the save. No burst, no verdict. A failed save is silent. | On finish, play the burst and open the verdict sheet. Haptic after success, `reject` on failure. | M |
| S9 | Reading | **The next book cuts into the hero.** (`ReadingBooksColumn.kt:60`, `MarkAsReadController.kt:94-116`) | The key changes, so the new book appears in one frame. | It rises into the hero, and its cover travels up from its row. | M |
| S10 | Reading, Profile | **Streak milestones pass unmarked.** (`StreakStrip.kt:44-66`, `ReadingAtlasMapper.kt:40-48`, `StatTile.kt:144-187`) | Today's dot snaps on. Day 30 looks like day 2. `milestone()` is never used for streaks. | Animate the dot. At 7, 30, 100 and 365 days, fire `milestone` once with a small burst. | M |
| S11 | Reading | **The peek bar is flat.** (`SessionPeekBar.kt:70-155`) | No press scale or hover. Play and pause swap in one frame. | Press scale and hover. Crossfade the glyph and the elapsed line. | S |
| S12 | Focus Mode | **Paused looks broken.** (`FocusReadingPanel.kt` `FocusReadingPanel`) | The colours and button snap. | Animate the timer colour, breathe slowly while paused, and crossfade the button. | S |
| S13 | Reading | **The session button swaps abruptly.** (`FeaturedSessionButton.kt:51-111`) | `when` | Crossfade and resize it in step with the peek bar. | S |
| S14 | Reading | **The empty and populated states cut.** (`ReadingScreenLayout.mobile.kt:100-131`, `.jvm.kt:37-71`) | The burst plays over a hard cut. | Crossfade the two bodies. | S |
| S15 | Reading | **The Trending tile has no press feedback.** (`TrendingTileSection.kt:19-26`) | It shows a hand cursor and nothing else. | Press scale and hover. | S |
| S16 | Reading | **Desktop rows jump up.** (`ReadingScreenLayout.jvm.kt:121-138`) | A non-lazy `Column`. | Collapse the removed row smoothly. | M |
| S17 | Reading | **The hero has no depth.** (`FeaturedBackdropCard.kt:103-115`) | It scrolls rigidly. | Parallax on the backdrop. Mobile only. | M |
| S18 | Book detail | **The burst plays under the verdict sheet.** (`BookDetailScreen.kt:91-97`) | Both are triggered in the same frame. | Play the burst first, then open the sheet. | S |
| S19 | Book detail | **Shelving never pulses the Library tab.** (`ShelveControlCard.kt:111-152`) | Only Reading pulses it. | Pulse the tab when a shelve is applied. | S |
| S20 | Book detail | **A status change rebuilds the lens in one frame.** (`YoursLensContent.kt:33-65`) | `when (status)`, and `VerdictBlock` pops in. | A size-aware crossfade and a vertical reveal. | S |
| S22 | Book detail | **The top-bar title pops in, and its scroll behaviour does nothing.** (`BookDetailScreenLayout.mobile.kt:50-104`) | Index threshold. The list has no `nestedScroll`. | Drive the title from scroll progress, and wire `nestedScroll`. | M |
| S23 | Book detail | **The hero scrolls like any other row.** (`GeneralBookInfoSection.kt:76-103`) | It moves 1:1 with the list. | Parallax on the backdrop. Android below 12 can't blur, so it needs a heavier dim. | M |
| S24 | Book detail | **The full-screen cover jumps to 2.5×.** (`CoverZoomState.kt` `CoverZoomState`, `.mobile.kt:23-35`) | No animation. No swipe to dismiss. | Animated zoom, swipe-down to dismiss, and soft edges. | M |
| S25 | Book detail | **The wide pane cuts between books.** (`BookDetailPaneHost.kt:33-47`, `BottomBarScreen.kt:163-184`) | `key(destination.id)` | A fade-through. | S |
| S26 | Book detail | **Shelve rows have no desktop affordances.** (`ShelveControlCard.kt:275-295`) | No hand cursor or hover. | Add both. | S |
| S28 | Explore | **Feed, focus and results cut.** (`ExploreScreenLayout.mobile.kt:120-140`, `.jvm.kt:114-126`) | `when (searchPhase)` | A fade-through, with the results settling upward. | S |
| S32 | Explore | **Search focus and dismiss rows ripple.** (`SearchFocusContent.kt:128,159`, `ContinueSeriesMenuSheet.kt:76`) | Plain `.clickable`. | A small press response. | S |
| S33 | Explore | **The result bookmark doesn't fill.** (`BookCardControls.kt:42-45`) | It changes colour only. | The animated selection-state glyph pop, plus `select` when adding. | S |
| S34 | Profile | **The page lands all at once.** (`ProfileReadingLife.kt:24-96`, `ReadingAtlasSection.kt:35-59`) | No stagger. | A one-time `StaggeredEntry` welcome. | S |
| S36 | Profile | **The bar never shows whose profile it is.** (`ProfileScreenLayout.mobile.kt:51-66`, `.jvm.kt:85-101`) | No scroll behaviour. | Lift the bar on scroll and fade the name into it. | M |
| S37 | Onboarding | **Sign-in cuts straight to the app.** (`OnboardingScreen.kt:42-44`, `LoadingSheet.kt:42-47`, `orch/App.kt:137-145`) | `key(authenticated)` swaps while the sheet is still open. Desktop shows a blank frame. | `commit`, then a crossfade. A wordmark on desktop's first frame. | M |
| S38 | Settings | **A dropped tab jumps into its slot.** (`ReorderableTabsGroup.kt:148-180`) | The offset resets to 0. | Spring the tab into its slot, and fire `drop` when it lands. | S |
| S39 | Settings | **Hiding a tab snaps.** (`ReorderableRow.kt:55,72,199`) | The alpha jumps and the label swaps. | Animate the alpha and crossfade the label. | S |
| S40 | Settings | **Saving tabs isn't confirmed.** (`LibraryVisibilitySaveBar.kt:29-51`) | The button just disables. | Show "Saved" briefly with `select`. | S |
| S41 | Settings | **The update callout pops in.** (`SettingsScreenLayout.mobile.kt:101-114`, `AboutPane.kt:25`) | `?.let` | Expand and fade it in. | S |
| S42 | Onboarding | **Paste from clipboard ripples.** (`ApiKeyEntrySection.kt` `ApiKeyEntryContent`, `PasteFromClipboardRow`) | Plain `clickable`. | Press scale. A paste fires `select` and highlights the field. | S |
| S43 | Settings | **Sub-pages scroll under a flat bar.** (`AppearanceSettingsScreenLayout.mobile.kt:27-52`, `LibraryVisibilitySettingsScreenLayout.mobile.kt:30-60`) | No `scrollBehavior`. | A pinned scroll behaviour. | S |
| S44 | Onboarding | **The page number lags the swipe.** (`OnboardingScreenLayout.mobile.kt:163-190`) | It snaps when the page settles. | Drive the tint from the pager offset. | S |
| S45 | Settings | **Desktop categories cut.** (`SettingsScreenLayout.jvm.kt:74`) | `when (selected)` | A fade-through. | S |
| S46 | Scanner | **A successful scan isn't marked.** (`BarcodeScannerScreen.kt:64-81`, `.mobile.kt:91-97`) | The camera is replaced by a grey spinner. No haptic. | `threshold`, the reticle tightens, the frame is held under a scrim, then the root push transition. | M |
| S47 | Scanner | **The scanner doesn't look like it's searching.** (`BarcodeScanner.android.kt:96-146`, `.ios.kt`) | A static rectangle, dimmed only at the top. | A cut-out scrim and a slow pulse on the reticle. | S–M |
| S48 | Scanner | **Switching to manual entry cuts.** (`BarcodeScannerScreenLayout.mobile.kt:52-78`) | Hard swaps. | Crossfade. | S |
| S49 | Share | **Switching card variants swaps hard.** (`ShareCardSheetContent.kt:142`, `ShareBookBottomSheet.kt:22-37`) | The sheet height jumps. | Crossfade and animate the size. | S |
| S50 | Share | **The preview looks flat.** (`ShareCard.kt:97-100`, `ScaledShareCardPreview.kt`) | No shadow. | A shadow on the preview host only, never on the captured card. | S |
| S51 | Desktop nav | **The sidebar selection snaps.** (`EditorialSidebar.kt:99-125`) | No hover or hand cursor. | Animate the selection, and add hover and a hand cursor. | S |

## Open suggestions

| # | Suggestion | Touch | Eff |
|---|---|---|---|
| O16 | **Ending a session says nothing.** (`FocusReadingPanel.kt` stop button) | A recap ("42 min · 18 pages") with a quick page update. | M |
| O17 | **Focus Mode has no steppers.** (`FocusPageEditor.kt` `FocusPageEditor`) | Reuse the `StepperCircle` row, and show pages read this session. | S–M |
| O18 | **"Also reading" rows have no quick actions.** (`ReadingCardEventHandler.kt:27-30`) | A long-press or right-click menu: start session, mark as read, update progress. | M |
| O19 | **Bulk actions pause silently.** (`OnBulkMoveShelfAction.kt:37-56`) | Show "3 of 12" in the selection header. | S |
| O20 | **Creating a list gives no feedback.** (`CreateListScreen.kt:38-53`) | Progress in the button and `commit` on success. | S |
| O21 | **Book detail and Profile have no pull-to-refresh.** | `PullToRefreshEyebrow`. Mobile. | M |
| O22 | **A saved rating doesn't visibly land.** (`cc/verdict/VerdictBlock.kt:103-120`) | A one-time star sweep. | M |
| O23 | **Hero metadata snaps on an edition change.** (`GeneralBookInfoSection.kt:212-341`) | Crossfade it. | S |
| O24 | **The desktop detail sidebar has no scrollbar.** (`BookDetailScreenLayout.jvm.kt:141-146`) | `DesktopVerticalScrollbar`. | S |
| O25 | **Long descriptions are a wall of text.** (`AboutSection.kt:61-76`) | Collapse behind "Show more". | S |
| O26 | **Rails stop mid-card.** (`ExploreCardRail.kt:35-39`) | Snap to whole cards. | S |
| O27 | **Mood results lose the mood's colour.** (`OnMoodChipClickAction.kt:44`) | Carry the mood's colour into the results. | M |
| O28 | **Load-more rows pop in.** (`SearchResultsSection.kt:117-126`) | Fade in the new rows. | S |
| O30 | **Desktop has no keyboard or mouse-back navigation.** (`desktopApp/.../Main.kt`) | Tab shortcuts, back shortcuts and a minimal MenuBar. | M |
| O31 | **The scanner has no torch.** (`BarcodeScanner.android.kt:50-76`) | A torch toggle. | S–M |
| O34 | **A progress update gives no cue where it landed.** (`ReadingOverlays.kt:57-84`) | Briefly highlight the updated row. | S |
| O37 | **A zero streak looks live.** (`ReadingAtlasMapper.kt:40-48`) | A muted "Start today" state. | S |
| O39 | **First-login progress stalls at 20 %.** (`OnApiKeySaveClickAction.kt:47-63`) | Report real stages. | M |
| O40 | **The onboarding hero appears all at once.** (`OnboardingScreenLayout.mobile.kt:192-255`) | A one-time stagger. | S |
