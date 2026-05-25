# DESIGN_SYSTEM.md

The visual and interaction language for Softcover. This document is purely descriptive — it does not point at code. When a screen is being designed or redesigned, a contributor should be able to read this and know how the surface should look and behave without consulting other screens.

> **Maintenance rule.** Any change that introduces, retires, or alters a foundation, component, or pattern in this system must update this file in the same change. If the doc and the code disagree, the code is wrong, the doc is wrong, or both — none of those is acceptable to merge.

---

## 1. Tone

The system is **editorial**: it borrows from print magazines and book covers rather than from utility dashboards. Surfaces feel like spreads. Type does heavy lifting; chrome is restrained. Italic display faces and a single primary accent carry character. Decoration is sparing — a thin accent bar, a quote glyph, a wavy progress indicator — and always serves a hierarchy decision, not ornament for its own sake.

The reader is the protagonist. Their books, progress, and stats are foregrounded; app affordances retreat.

## 2. Foundations

### 2.1 Color

Color is sourced from the Material 3 expressive scheme; this section defines the *roles* those tokens play in this app.

- **Primary.** The accent. Used for emphasis on text and chrome (eyebrows, key stats, the leading mark of a section, progress indicators, cursors, active toggle states, a button's filled style), and — deliberately — as the **surface fill of a hero stat card**: the page-opening stat tile (e.g. the profile screen's pages-read hero). At most one primary-filled card per scrollable region. Inside such a card the content colour is `onPrimary`; demoted text drops to `onPrimary` at ~0.75 alpha; the divider drops to `onPrimary` at ~0.25 alpha; if the card needs a call-to-action it steps down a tier (elevated or tonal) so it doesn't disappear into the surface. This treatment is reserved for stat heroes — cards built around imagery (covers, photos) keep a `surfaceContainer*` shade so the imagery, not the chrome, carries the colour. Beyond the stat hero, primary is **not** a background colour — ordinary cards, tiles, sections, and grouped regions use page background or a `surfaceContainer*` shade.
- **On-surface / on-background.** Body and headline copy. Default for almost all readable type.
- **On-surface-variant.** Demoted copy: suffixes, captions, secondary metadata, inactive labels. Use to push information back without losing legibility.
- **Surface / background.** Page canvas. Plain.
- **Surface-container, surface-container-high, surface-container-highest.** Elevated tiles, cards, and grouped sections. Step up the container shade to express grouping or focus, not depth in pixels — elevation is communicated by tone, not by drop shadows.
- **Surface-container-lowest.** Reserved for modal sheets and overlays that sit *above* the page rather than within it.
- **Outline / outline-variant.** Hairline dividers and the rare bordered control. Avoid dense rule lines; prefer whitespace and tone changes.
- **Inverse-primary.** Notification dots and small attention markers on chrome.
- **Error / on-error.** Destructive and validation states only.

Dynamic color (Android 12+ Material You) is supported but **off by default**. The editorial scheme is the canonical look; dynamic color is an opt-in personalisation, not the design.

### 2.2 Typography

Two type families carry the system, split by voice:

- **Fraunces** — the editorial serif. A variable face with true italics; carries every role where the editorial voice should be heard: page titles, display headlines, italic headlines, italic body prose, hero stats, and the decorative quote glyph. Stats use Fraunces with tabular figures so digits don't jitter when numbers animate.
- **Inter** — the chrome sans. Carries everything that should retreat: eyebrows (all-caps with wide tracking), card and list titles, Material component internals (top-bar titles, button labels, navigation labels, dropdown items), and any utility label. Inter is roman in this system; if a chrome surface needs italic, that is a sign the role belongs in the editorial scale instead.

Two parallel scales coexist:

- **Material typography** — the standard body/title/label scale used inside off-the-shelf Material 3 components. Components consume this implicitly; do not override it for component internals.
- **Editorial typography** — a project-specific scale, accessed alongside Material typography, used for any text the *screen itself* composes. Roles below.

| Role            | Where it goes                                                                 | Character                          |
| --------------- | ----------------------------------------------------------------------------- | ---------------------------------- |
| `eyebrow`       | All-caps section labels, kicker over a headline, accent over a hero stat     | Wide letter-spacing, semibold      |
| `eyebrowSmall`  | Same role, when packed inside cards/rows                                     | As above, tighter                  |
| `pageTitle`     | The screen's primary title rendered *inside* the scrolling page (not the top-bar slot — that uses Material `titleLarge`). Reach for it on root screens whose top bar is transparent or absent. | Roman, semibold, large |
| `display`       | Section headlines that follow an eyebrow                                     | Italic, semibold, expressive       |
| `headlineMedium` / `headlineSmall` | Subsection or modal headlines                                  | Italic, semibold                   |
| `titleLarge` / `titleMedium` / `titleSmall` | Card titles, list item titles, grouped row headers       | Roman, semibold                    |
| `bodyLarge` / `body` / `bodySmall` | Running prose, descriptions, captions                            | Italic                             |
| `statHero`      | The single largest number on a screen (e.g. a hero count)                    | Italic, semibold, oversize         |
| `statLarge`     | Editable hero numbers (input fields styled as stats)                          | Italic, semibold                   |
| `quoteGlyph`    | Decorative oversized quotation mark — used either behind an actual quote block or as an editorial flourish in an empty state. Tinted `onSurfaceVariant` at low alpha. | Display face, very large, low alpha |

Italic is a deliberate signal of editorial voice; it is reserved for display, headlines, body prose, and stats. Roman is reserved for page titles, card/list titles, and component chrome — places where italic would feel decorative rather than deliberate.

Never inline an ad-hoc `TextStyle`; pick the closest editorial or Material role and `.copy(...)` only when a documented exception requires it (e.g. tinting `quoteGlyph` with a low alpha).

### 2.3 Shape & elevation

- **Corners.** Small radii on cards and tiles; medium on buttons (Material defaults via expressive theme); larger, fully-rounded for the active state of toggle buttons. Cover art uses a subtle radius to hint at a printed object without softening it into a sticker.
- **Elevation.** Tonal, not shadowed. Surface-container shades replace drop shadows for grouping. The one exception is book covers, which carry a slight shadow to read as physical objects on the page.
- **Accent bar.** A short, primary-coloured bar in two sizes, picked by the role of the eyebrow it leads:
  - **Section bar — 32×4 dp.** The "this is a section / this is a sheet" mark. Sits in the page gutter (not inside a card) and precedes a full-size `eyebrow` + display/headline pair. Used by sheet headers, settings section headers, and any top-level region introduction.
  - **Inline bar — 20×1 dp hairline.** The smaller-scale variant for an eyebrow living *inside* a card, hero region, or subcomponent. Always paired with `eyebrowSmall`, never with the full `eyebrow`. Used on featured cards (Reading), page-title accents (Library), and inline status callouts (Book detail).
  Never mix sizes: a full `eyebrow` always gets the 32×4 bar; an `eyebrowSmall` gets either the 20×1 hairline or no bar at all.

### 2.4 Spacing

Spacing is rhythmic, not formulaic. The recurring values are:

- **4 / 8** dp — tight inline gaps (icon-to-label, badge offsets).
- **12 / 16** dp — within-component padding, label-to-control gaps.
- **20 / 24** dp — page horizontal padding, gap between a card's image and its text block.
- **28 / 32** dp — gap between distinct content blocks inside a sheet or hero region.
- **40 / 48** dp — gap between major page sections.

Sections always breathe. Two adjacent eyebrow/headline pairs without a generous gap between them is a layout bug.

### 2.5 Motion

- Use the Material **expressive** motion scheme; do not hand-author durations or easings unless animating a custom property the scheme cannot express.
- **Press feedback.** Tappable cards, primary (filled) buttons, and ripple-less hero surfaces scale to ~0.97 while pressed and ease back to 1f on release. The scale rides an `InteractionSource` so it lines up with the platform's pressed-state ticks; it is wired through `Modifier.pressScale` (a press-driven `graphicsLayer` scale) or its `Modifier.pressScaleClickable` convenience (which bundles a ripple-less `clickable`). Suppressed under reduced motion (§2.5 gate). Press scale is the *only* indication on ripple-less surfaces — never strip the ripple from a card or hero without adding the scale, or the surface loses its press feedback entirely. Tonal/outlined/text/elevated buttons keep the Material ripple + pressed-shape morph and do not stack scale on top; a single button surface only carries one press treatment.
- **Press haptics.** A *commit* haptic fires only on commit-class actions (mark-as-read, save, confirm) — never on navigation taps (tapping a book card to open detail, tapping a row to push a sub-page). Press scale is purely visual; it never implies a haptic.
- Progress is shown with the wavy progress indicator — flat bars are reserved for chrome (top-bar scrim) and never carry semantic progress.
- Numeric progress animates between values rather than snapping.
- Sheets enter and dismiss from the bottom; overlays cross-fade.
- **Tab-root swap.** Switching between the four bottom-bar roots plays a 200ms cross-fade with a 12dp upward drift on the incoming tab — never a hard cut, never a horizontal slide (which would imply spatial relation between unrelated roots). Per-tab `rememberSaveable` state survives the swap via a shared `SaveableStateHolder` keyed by tab key. Suppressed under reduced motion (§2.5 gate) — in that case the previous instant swap is used. Sub-screens pushed onto a tab's stack continue to use the standard `ScreenTransition` (currently `None`); this rule applies only to the root-tab swap.
- **Mark-as-read commit choreography.** When a book transitions to the Read shelf via a user action (chip tap on book detail, "Mark as Read" menu item on the reading screen, or progress reaching 100%) the surface plays a ~800–1000ms burst: a primary-color particle burst (`MarkAsReadBurst`, §4), the chip's check-icon punches with a left-to-right reveal, the section-label accent bar pulses, and a single *commit* haptic fires. No other shelf transition (Want-to-read, Reading, DNF) earns the celebration — only Read. On the Reading screen specifically, the celebrated row also performs a "slide to shelf" follow-through: ~320ms downward translate (~96dp) + fade, played before the screen-model action dispatches; the Library bottom-bar icon pulses once in parallel (180ms peak to 1.22× scale, 240ms settle) via `BottomBarPulseManager.pulseLibrary()`, so the eye is led from the source row to the shelf where the book just landed. Both the slide and the pulse are suppressed under reduced motion (the action dispatches synchronously instead).
- **Mutation-rejection shake.** When an optimistic mutation rolls back, the affected card/row plays a single horizontal shake: 6dp amplitude, 80ms each half-period, 3 cycles, returning to centre. The card's eyebrow slot also swaps to the error role with a "Couldn't save — tap to retry" label for the duration of the failed state. Use the shared `Modifier.shakeOnError` for this — never hand-roll the animation.
- **Lazy-list add/remove animation.** Only user-triggered list mutations animate; initial load, background refresh, and pagination tail jump as today. Removals fade out and neighbours slide into the vacated space; additions fade in and play a brief 20×1 dp accent-bar pulse (§2.3 inline-bar hairline) at the top edge of the inserted item. The shared `rememberMutationAnimatedModifier` (§4) is the only way to wire this up — do not hand-roll `animateItem` per call site, and never wrap the item in a `Box` to host the modifier (the wrapper layout node causes stale lazy-item measurements).
- **Staggered entry on first composition.** Carousel and list items that compose during the initial screen-entry window play a brief upward translate (~8dp) + fade in, delayed by ~60ms per index. Items composed after the window (scrolled into view, swapped in by a background refresh, mutated in later) render statically — this is a one-shot welcome moment, never an "animate on scroll" effect. Wire it via the shared `rememberStaggeredEntryCoordinator` + `Modifier.staggeredEntry(coordinator, index)` (§4); compose it onto the item modifier alongside the mutation animator when both apply. Suppressed when system animations are disabled.
- **Cover-to-detail morph.** A tapped book cover on any source surface (library, explore carousels, reading) morphs into the book-detail hero cover via `Modifier.sharedBounds`. The morph is opt-in: pass `sharedTransitionKey = bookCoverTransitionKey(editionId, bookId)` to `EditionImage` at both source and destination. Both call sites must derive the same key — prefer edition id, fall back to book id. The destination paints the cover immediately from a `BookInitialCover` carried through navigation, so the morph never lands on an empty hero. The shared scopes (`LocalSharedTransitionScope`, `LocalNavAnimatedVisibilityScope`) are provided by the navigator shim in `RootScreen`; non-morphed pushes look identical to before. Do not apply the key to decorative `EditionImage` usages (e.g. blurred backdrops, full-screen cover viewer) — only to the tappable source cover and its detail hero.

### 2.6 Iconography

- Material outline icons, stroke weight consistent across the app. Never mix filled and outlined icon families on the same surface.
- Icon-only controls always carry a content description.
- Icon size scales with the control's size token (XS → XL); icons embedded in body type sit on the type baseline at a size proportional to the surrounding text.

## 3. Layout primitives

### 3.1 Page scaffold

A page has, top-to-bottom: optional top app bar, scrolling content with horizontal padding consistent for the whole page, and bottom navigation (when on a tab root). The bottom of scrolling content reserves padding equal to the bottom bar's footprint so the last item is never occluded.

Top bars are either a plain title bar (centered or start-aligned title, optional subtitle, optional back, optional trailing actions) or a search bar (text field with leading search/loading state and trailing clear). Both are sourced from the shared top-bar components — no screen rolls its own.

**Root tabs prefer an in-page editorial title over a chrome title.** Reach for `SoftcoverTopBar` with a plain `title` only when the screen needs back navigation or trailing icon actions — i.e. on sub-screens pushed into the stack (settings sub-pages, profile, book detail). On a root tab, the screen's identity is rendered *inside* the scroll using the `pageTitle` editorial role (optionally paired with a body subtitle), so it shares the editorial scale with the section headers below it. The Material `titleLarge` chrome title reads as system chrome and clashes with the editorial section rhythm that follows. Library and Settings both follow this rule; Explore is title-less by design (its search bar is the chrome).

### 3.2 Section rhythm

A content section is composed of:

1. **Eyebrow** — short all-caps label in primary colour.
2. **Headline** — italic display headline directly underneath the eyebrow, on-surface colour.
3. **Body** — the section's content: a horizontal carousel, a vertical list, a grid, or a single block.
4. Generous vertical gap before the next section.

This is the canonical pattern. Anywhere a screen needs to introduce a region of content, it uses this triplet — the eyebrow gives the editorial label, the headline gives the human-readable title, and the body delivers.

### 3.3 Hero region

A hero region opens a screen with a single dominant element: a stat, a quote, or a featured cover. Hero text uses `statHero`, `display`, or a quote pattern (oversized low-alpha `quoteGlyph` behind italic headline). Hero regions sit on the page surface, not on a container — they are part of the page, not a tile within it.

### 3.4 Carousels and cards

Horizontal carousels are the default for collections of books. Cards inside a carousel are fixed-width and fixed-height. Optional rows inside a card (rating, badge, secondary line) reserve their height even when empty so cards do not jump as data resolves.

Horizontal carousels do not paint an explicit overflow affordance (no edge bar, no chevron, no fade). The rightmost card sitting partially clipped against the screen edge — paired with the standard 24dp content padding so it never butts flush — is itself the "there's more" cue. Do not reintroduce a page-edge hint, scrollbar, or page indicator on book carousels.

Card anatomy (top-to-bottom):

1. Cover image (book proportions, subtle radius and shadow).
2. Title in `titleMedium` or `titleSmall`, max two lines.
3. Optional metadata strip in `eyebrowSmall` or `bodySmall`, on-surface-variant.
4. Optional trailing accent (rating, progress fragment) in primary colour.

Vertical lists use the same anatomy laid horizontally: cover at the leading edge, text block taking the remaining width.

### 3.5 Modal sheets

Modal bottom sheets open in a fully-expanded state on `surface-container-lowest`. The first element inside is always the canonical header pattern — accent bar, eyebrow, headline (and optional editorial description line) — followed by the sheet's body content. Sheets handle their own dismissal; they never require a custom close button.

**This rule has no exemptions.** Every modal sheet — pickers, action sheets, progress sheets, blocking loaders that happen to be implemented as a sheet — opens with the editorial header. A sheet's job is to feel like a small editorial spread, not like a Material dialog. Material `titleLarge` / `bodyMedium` headers signal "system chrome" and read as a regression; reach for `eyebrow` + `display`/`headlineMedium` + `body` instead. If a sheet's content is so transient that an editorial header feels theatrical, that is a signal it should be a `Dialog` (an unobtrusive overlay), not a sheet.

**Worked example — blocking loading sheet.** Onboarding fetches the user's library; the wait can be long, so the surface is a non-dismissable `ModalBottomSheet` rather than a tiny dialog. The sheet still opens with `EditorialSectionHeader(eyebrow = "Setting up", headline = "Pulling your library together.", description = "Depending on its size, this might take a moment.")`, then a wavy progress indicator below. Container is `surfaceContainerLowest`; back-press and scrim-tap dismissal are disabled, but no close button is added. The result reads like a printed cover-page status line, not a system spinner card.

## 4. Components

A short catalogue of shared components and what role they play. Anatomy and styling live with the components; this list is so a designer or contributor knows *which* tool to reach for.

- **Top app bar** — page title, optional subtitle, optional back, optional trailing icon actions. Title autosizes within `titleLarge`.
- **Search top app bar** — same skeleton, replaces title with a single-line search field; leading icon flips to a wavy circular indicator while loading; trailing icon appears once the user has typed.
- **Bottom navigation** — tab destinations for the four roots. Provided as a docked bar (full-width, attached to the system inset) and a floating toolbar variant; pick docked unless the screen explicitly calls for a hovering bar over scrolling media.
- **Button** — five styles (filled, tonal, elevated, outlined, text) across five sizes (XS → XL). Filled is the page's primary action; tonal is a secondary action that still wants weight; outlined and text are tertiary. One filled action per region — never two competing primaries.
- **Toggle button / icon toggle button** — same size scale as Button; the active state expresses itself via shape morph and fill, not via a separate badge.
- **Split button** — a single primary action paired with an attached chevron that opens a menu of related variants (e.g. "Mark as reading" + alternative shelves). Reach for it when an action has one obvious default and a small set of equivalents.
- **Edition image** — the canonical book cover. Always 2:3 aspect, subtle radius, optional soft shadow tinted by edition. Falls back through edition → default edition → URL → shimmer placeholder. Never use a raw image loader for covers.
- **Loading dialog** — full-screen blocking wavy progress indicator for unavoidable waits; prefer inline shimmer on cards when the wait is short or the surface can stay interactive.
- **Pull-to-refresh indicator** — the Material 3 Expressive `ContainedLoadingIndicator` inside a `PullToRefreshDefaults.IndicatorBox` is the canonical pull-to-refresh affordance. This is a deliberate exception to the "wavy progress, always" rule (DS §2.5, §6): the wavy circular indicator visually competes with the pull gesture's own arc and reads as off at this size, while the contained loading indicator's morphing shape carries the same expressive character without the conflict. Reach for the wavy primitive everywhere else progress is shown.
- **Pull-to-refresh eyebrow** — `PullToRefreshEyebrow` is the canonical eyebrow for any screen that hosts a pull-to-refresh surface. It renders the same accent-bar + eyebrow anatomy as a static `SectionLabel`, but swaps the label to a contextual refresh copy ("Refreshing your shelf…", "Catching up on your reading…") while the user pulls and during the subsequent refresh, then flashes the base label briefly in italic on revert. Wired internally: the eyebrow consumes the same `PullToRefreshState` already passed to `PullToRefreshBox`, listens to its `distanceFraction`, and fires a single `threshold` haptic at the trigger crossing (re-armed once the gesture relaxes). Reach for it on every refreshable root surface; do not hand-roll the eyebrow swap or the trigger haptic at call sites.
- **Softcover image** — the canonical loader for non-cover images (avatars, author photos, any free-aspect imagery). Use this rather than reaching for a raw image library; it shares the project's caching, shimmer, and fallback behavior. `EditionImage` is for book covers only — everything else uses `SoftcoverImage`.
- **Update progress sheet** — modal sheet for editing reading progress. Hosts the canonical `pages | percentage | time` segmented switcher with a hero numeric input, an editorial suffix line, and a wavy progress bar. New "edit a single number on a book" surfaces should look at this sheet before inventing a new one.
- **Deadline badge / cover overlay / summary line** — the family of components that mark a book as time-bound. Used together when a book's deadline matters; never mixed with a stale rating row in the same card.
- **Unreleased badge** — `UnreleasedBadge` is the canonical mark for a book whose effective release date is in the future. Two styles: `Compact` ("Out MMM d", with year appended only when not the current year) sits as a top-start overlay on cover art in carousels and grid cards; `Prominent` ("Releases MMMM d, yyyy") sits inline beneath the hero byline on book detail. Surface is `primary` / `onPrimary` so the chip reads as a deliberate publishing signal rather than another deadline state. When the prominent variant is shown on book detail, suppress the trailing release year in the byline so the date isn't named twice. The "is it unreleased?" check is centralised on `Book.isUnreleased` — never compare dates at the call site.
- **Haptics helper** — the single entry point for haptic feedback. Exposes eight semantic cases. No ad-hoc haptics — never call `performHapticFeedback` from a call site; always go through the helper. The "no mixed icon families" rule (§2.6) has a sibling here.
  - **commit** — a user-triggered action succeeded (mark-as-read, save, confirm). Celebratory in texture; reserved for commit-class actions, never navigation taps.
  - **reject** — an optimistic mutation rolled back, or an action refused. Pair with the shake-on-error visual (§2.5).
  - **select** — a soft tick on neutral selection: shelf chip toggle to a non-read state, segmented switch in a sheet, tab change in a carousel filter. Distinct from *commit* (which celebrates) and *reject* (which rolls back).
  - **threshold** — a single firm tap when the user crosses a meaningful boundary: pull-to-refresh trigger point, drag-to-reorder pickup, long-press peek activation. No celebratory texture.
  - **tickle** — a per-integer tick during a slider or picker drag. Each integer crossing on the rating control or progress page-number input fires one tick.
  - **lift** — drag-to-reorder pickup. Says the item has left the page; pair with the visual lift.
  - **drop** — drag-to-reorder settle. Distinguishable from *lift* so the item's return to the page is felt.
  - **milestone** — a two-pulse haptic above *commit* for natural progress events: completing a year-end reading goal, hitting a 30-day streak, finishing the last book in a series. Reserved for moments that are bigger than a save.
- **Mark-as-read celebration** — `MarkAsReadBurst` is the canonical hero moment for a successful mark-as-read commit. It paints a radial particle burst (primary + tertiary tinted) outward from the centre of its footprint; replays whenever `triggerKey` changes; suppressed under reduced-motion (§2.5). Compose it as a sibling overlay (`Modifier.matchParentSize()` or `Modifier.fillMaxSize()`) on whichever surface owns the commit — the shelf-action card on book detail and the reading screen at full-screen scale. Always pair the trigger with a single *commit* haptic so the visual and haptic land together. Do not reach for it on other status transitions — read is the only shelf change that earns a celebration.
- **Animated stat number** — `AnimatedStatNumber` is the canonical way to render a numeric stat that can change in place (profile stats, pages-read counters, deadline pacing). Tweens between values via `animateFloatAsState`, forces tabular figures so digits don't jitter mid-tween, and snaps when the user has disabled system animations (§2.5). Each integer crossing during the tween fires a brief 1dp hairline pulse below the number, tinted with the same content colour as the number itself so the tick reads against any surface (including the primary-filled hero card, where the number is `onPrimary`) — a quiet ledger tick that makes a count feel earned rather than rolled; suppressed under reduced motion. Reach for it whenever a number on screen is bound to live state — pair it with the wavy progress indicator under the Hero stat pattern (§5) so bar and number move as one motion. Raw `Text` for a number that can change should be treated as a bug.
- **Lazy-item mutation animator** — the canonical wiring for animating add / move / remove on a lazy list when the change is user-triggered. `rememberLazyItemMutationAnimator(keys)` snapshots the initial set of keys on first non-empty composition; `rememberMutationAnimatedModifier(animator, itemKey)` returns a `Modifier` (carrying `Modifier.animateItem()` plus a `drawWithContent` accent-bar pulse) that the caller applies to the outermost composable of each lazy item. Apply directly — never via an intermediate `Box`, since an extra layout node interferes with lazy-item measurement. Items added or removed after the snapshot fade and reflow; new items also get a brief 20×1 dp accent-bar pulse (§2.3 inline-bar hairline) at their top edge. Suppressed when system animations are disabled. Use it for shelves and chip rows where the user's own action is the "what changed" signal; do not apply it to carousels backed purely by server data (trending, continue-series).
- **Share card** — `ShareCard` is the single composable that renders any user-facing share artefact (book, hero stat, pulled quote, year-recap slide) into a fixed-aspect editorial composition with a `SOFTCOVER` folio sign-off. Variants are picked by `ShareContent` subtype: book (cover + title + byline + optional rating + optional quote), stat (`primary`-filled hero with eyebrow + oversize numeral + caption), quote (oversize low-alpha `quoteGlyph` behind italic headline + primary byline), year-recap (eyebrow + display + bulleted highlights). Dimensions live in `ShareCardDimensions`; never override them at the call site. Pair with `CapturableShareCard` + `rememberShareCardCapture` to render the card, then call `capture.saveToGallery(displayName)` to write a PNG into `Pictures/Softcover/` (MediaStore on API 29+, legacy public-Pictures dir + scan on API 26–28). The returned `SaveOutcome.Saved.uri` is a `content://` URI suitable for handing straight to an `ACTION_SEND` chooser; the gallery write is part of the card's contract, the share *intent* itself is the responsibility of the calling surface. Legacy storage permission (`WRITE_EXTERNAL_STORAGE`, `maxSdkVersion=28`) is requested through `rememberGalleryWritePermissionRequester` — on API 29+ the requester is a synchronous pass-through. Reach for this rather than building a new share image inline; one renderer, one editorial register, one save path.
- **Staggered entry coordinator** — the canonical welcome-moment animator for carousels and lazy lists. `rememberStaggeredEntryCoordinator()` captures the screen-entry timestamp; `Modifier.staggeredEntry(coordinator, index)` plays a ~240ms upward translate (~8dp) + fade in, delayed by ~60ms per index, but only for items composed within the coordinator's window (default 350ms). Items composed later render statically — never apply this expecting items to animate on scroll. Pair with the mutation animator on the same item modifier when both apply (`mutationModifier.staggeredEntry(...)`). Suppressed when system animations are disabled.
- **Drop-cap text** — `DropCapText` renders editorial body prose with a 3-line Fraunces drop-cap on the first letter, tinted `primary` by default. The drop-cap font size is `bodyStyle.lineHeight * 3 * 0.92`; the first three lines of body text indent past the cap, and lines four onward resume at full width. Skips leading whitespace and punctuation when picking the letter; falls back to a plain `Text` for empty or letter-less bodies. Reach for it on book-detail descriptions; do not apply it to inline metadata or to bodies inside cards where the cap would dominate the tile.
- **Swipe row actions** — `SwipeRowActions` wraps a list-layout row in a Material 3 `SwipeToDismissBox` and reveals a coloured background as the user drags. Swipe-right (start → end) is the *mark-as-read* action — tertiary container with `ic_bookmark_check` and "Mark as read" copy; suppressed via `allowMarkAsRead = false` when the book is already in Read. Swipe-left (end → start) is the *remove* action — error container with `ic_delete` and "Remove" copy. Background opacity tracks the swipe progress (`progress * 1.2f`, capped at 1) so the affordance reveals as the user pulls. The dismiss state is reset immediately on commit so the row snaps back; the calling surface is responsible for the data write and for shaking the row on failure (pair with `Modifier.shakeOnError` on the wrapper). Reach for it on the `LIST_COMPACT` and `LIST_LARGE` library layouts; never apply it to grid cards.

## 5. Patterns

Recurring recipes that compose the primitives above.

- **Editorial section.** Accent bar (optional) → eyebrow → display headline → body (carousel, list, or block). The default way to introduce any region.
- **Hero stat.** A single oversize number in `statHero` or `statLarge`, an editorial suffix beneath in italic body, on-surface-variant. Used for "books read this year", "pages today", and progress sheets. The unit (e.g. "pages", "books") is named in the eyebrow above the number, never trailed next to it — pairing a unit suffix with the oversize numeral fights for horizontal space and breaks once the number grows. The number itself uses single-line autosize down from `statHero` so power-reader counts (six- or seven-digit page totals) still fit the card without reflowing the layout. The numeral is rendered via `AnimatedStatNumber` (§4) so changes tween in place — raw `Text` for a live-bound stat is a bug.
- **Editable hero stat.** Same as hero stat, but the number is a borderless `BasicTextField` styled with `statLarge`. Cursor is in primary colour. Width is computed from character count so the field does not jump as digits are typed.
- **Editorial quote.** Oversized low-alpha `quoteGlyph` (tinted `onSurfaceVariant`) behind an italic headline-medium pull quote, with a primary-coloured byline beneath. Used to surface notes, reviews, or a featured passage. The same glyph treatment also serves as a decorative flourish in empty states (e.g. "no books currently reading"), where the quote text below is replaced by an italic encouragement headline and no byline. In empty states only, the glyph plays a slow infinite ±2° sway (~6.8s each direction, FastOutSlowIn) via `Modifier.quoteGlyphSway()` — a magazine pull-quote breathing, never a marquee. Do not apply the sway to populated quote cards (review cards, share cards) — content next to the glyph fights a moving mark for attention. Suppressed under reduced motion (§2.5 gate).
- **Reserved-row card.** Carousel and grid cards reserve vertical space for every optional row they may show, so the row of cards never reflows as data streams in.
- **Tonal grouping.** When two regions need to feel distinct on the same page, step the container shade rather than drawing a divider or adding a card outline.
- **Inline filter chip strip.** A horizontally scrolling `Row` of pill-shaped chips placed directly above a grid or list to scope its contents (e.g. the Library "Read" tab's year chip row). Selected chip swaps to `secondaryContainer` / `onSecondaryContainer`; unselected sits on `surfaceContainer` / `onSurfaceVariant`. Always include a leading "All …" chip that clears the filter. Reuse the same chip anatomy as the shelf tab pills (§3.4); do not apply the carousel page-edge serif hint (§3.4) — the chip strip is a filter affordance, not a peek-affordance.
- **Active-filter chip row.** Distinct from the inline filter chip strip above: this row renders only the *currently applied* filters as removable input chips, with the picker living in a separate modal sheet. Each chip is `secondaryContainer` / `onSecondaryContainer`, the label sits on the start, and a trailing `ic_close` glyph (14dp) marks it removable on tap. Chips enter and leave via `expandHorizontally` + `fadeIn` / `shrinkHorizontally` + `fadeOut` so the mutation reads as part of the surface's editorial register, not as a pop-in. When more than one chip is active, append a tonal "Clear all" chip on `surfaceContainer` / `onSurfaceVariant` as the row's final element. Reach for this row whenever the picker UI is decoupled from the active set — multi-facet library filters, future faceted searches, anywhere the question "what's narrowing my view right now?" needs to be one-tap reversible. The picker itself follows §3.5's modal sheet anatomy with one `FacetSection` per facet (Title label + horizontal pill row), populated only from facet values actually present in the surface's source content so chips never narrow to empty.
- **Inline pacing nudge.** A dismissable one-liner in italic body copy tinted `primary`, placed in-flow above the card it speaks to. Used on the Reading screen to surface a per-day pacing target derived from an active deadline. Carries a trailing icon-button close affordance (no copy beyond the message itself). Persistence is per-book-per-day; once dismissed, the row stays gone until the local date rolls over. Reach for it when a single sentence of guidance derives from existing state and should not earn the weight of a card or sheet.
- **Adaptive empty state.** When a root surface is empty, surface the next-best content rather than a static "nothing here" panel. The empty Reading screen demonstrates the rule: if Want-to-Read has entries, render the top three as a "Pick up next" tile row (96dp covers + title only); else, render a single "Trending now" tile that links into book detail. Pair with the existing empty-state quote glyph + headline (§5 editorial quote, empty-state variant); do not stack three carousels — the empty state is a moment of orientation, not a discovery surface.
- **Drag-to-reorder list.** A tonal-grouped `Column` (surfaceContainer / RoundedCornerShape(20dp)) of rows, each prefixed with a leading `ic_drag_handle` glyph in `onSurfaceVariant`. Pressing the handle picks the row up immediately (no long-press; the handle *is* the affordance) — fire `Haptics.lift()` (§2.5), tint the handle `primary`, and translate the row vertically with the drag via `graphicsLayer { translationY }`. The other rows stay where they are during the drag; the row does **not** snap into intermediate slots while the finger is down. On release, compute the target slot from the cumulative offset (walking neighbouring row heights until the offset is consumed past the midpoint of each), commit the new order, and fire `Haptics.drop()`. Gesture wiring uses `Modifier.draggable(orientation = Vertical, startDragImmediately = true)` so it composes with a vertically-scrolling parent without fighting it. Long-press *outside* the handle (e.g. on a tab pill in the Library strip) is the canonical shortcut into the reorder screen — fire `Haptics.threshold()` and route to the canonical Settings entry rather than spawning an inline reorder mode. Used today for the Library tabs list under Settings → Library; will be reused by the in-list book reordering (Step 2.7).

## 6. Decision rules

When a new surface is being built, walk this list before reaching for novelty.

- **Need a primary action?** Filled button. One per region.
- **Need an action with a small set of variants?** Split button.
- **Need to edit a single bounded number?** Hero stat field inside a modal sheet.
- **Need to introduce a region of content?** Editorial section (eyebrow + headline).
- **Need to mark a region as elevated relative to its neighbour?** Step the container shade. Do not add a shadow.
- **Need an accent on a piece of text?** Use primary colour on the eyebrow or a leading word. Do not bold or recolour body copy.
- **Need a divider?** First try a vertical gap or a tone change. Reach for a hairline divider only when neither will do.
- **Need a custom font weight, size, or italic toggle?** Pick a different role from the editorial scale instead.
- **Need to show progress?** Wavy progress indicator. Always.
- **Need a modal interaction?** Bottom sheet first; full-screen second; dialog only for unavoidable blocking.
