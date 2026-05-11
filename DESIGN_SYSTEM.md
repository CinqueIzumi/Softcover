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
- Progress is shown with the wavy progress indicator — flat bars are reserved for chrome (top-bar scrim) and never carry semantic progress.
- Numeric progress animates between values rather than snapping.
- Sheets enter and dismiss from the bottom; overlays cross-fade.
- **Mark-as-read commit choreography.** When a book transitions to the Read shelf via a user action (chip tap on book detail, "Mark as Read" menu item on the reading screen, or progress reaching 100%) the surface plays a ~800–1000ms burst: a primary-color particle burst (`MarkAsReadBurst`, §4), the chip's check-icon punches with a left-to-right reveal, the section-label accent bar pulses, and a single *commit* haptic fires. No other shelf transition (Want-to-read, Reading, DNF) earns the celebration — only Read.
- **Mutation-rejection shake.** When an optimistic mutation rolls back, the affected card/row plays a single horizontal shake: 6dp amplitude, 80ms each half-period, 3 cycles, returning to centre. The card's eyebrow slot also swaps to the error role with a "Couldn't save — tap to retry" label for the duration of the failed state. Use the shared `Modifier.shakeOnError` for this — never hand-roll the animation.
- **Lazy-list add/remove animation.** Only user-triggered list mutations animate; initial load, background refresh, and pagination tail jump as today. Removals fade out and neighbours slide into the vacated space; additions fade in and play a brief 20×1 dp accent-bar pulse (§2.3 inline-bar hairline) at the top edge of the inserted item. The shared `rememberMutationAnimatedModifier` (§4) is the only way to wire this up — do not hand-roll `animateItem` per call site, and never wrap the item in a `Box` to host the modifier (the wrapper layout node causes stale lazy-item measurements).
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
- **Softcover image** — the canonical loader for non-cover images (avatars, author photos, any free-aspect imagery). Use this rather than reaching for a raw image library; it shares the project's caching, shimmer, and fallback behavior. `EditionImage` is for book covers only — everything else uses `SoftcoverImage`.
- **Update progress sheet** — modal sheet for editing reading progress. Hosts the canonical `pages | percentage | time` segmented switcher with a hero numeric input, an editorial suffix line, and a wavy progress bar. New "edit a single number on a book" surfaces should look at this sheet before inventing a new one.
- **Deadline badge / cover overlay / summary line** — the family of components that mark a book as time-bound. Used together when a book's deadline matters; never mixed with a stale rating row in the same card.
- **Haptics helper** — the single entry point for haptic feedback. Exposes two semantic cases only: *commit* (a user-triggered action succeeded) and *reject* (an action was refused or rolled back). No ad-hoc haptics — never call `performHapticFeedback` from a call site; always go through the helper. The "no mixed icon families" rule (§2.6) has a sibling here.
- **Mark-as-read celebration** — `MarkAsReadBurst` is the canonical hero moment for a successful mark-as-read commit. It paints a radial particle burst (primary + tertiary tinted) outward from the centre of its footprint; replays whenever `triggerKey` changes; suppressed under reduced-motion (§2.5). Compose it as a sibling overlay (`Modifier.matchParentSize()` or `Modifier.fillMaxSize()`) on whichever surface owns the commit — the shelf-action card on book detail and the reading screen at full-screen scale. Always pair the trigger with a single *commit* haptic so the visual and haptic land together. Do not reach for it on other status transitions — read is the only shelf change that earns a celebration.
- **Animated stat number** — `AnimatedStatNumber` is the canonical way to render a numeric stat that can change in place (profile stats, pages-read counters, deadline pacing). Tweens between values via `animateFloatAsState`, forces tabular figures so digits don't jitter mid-tween, and snaps when the user has disabled system animations (§2.5). Reach for it whenever a number on screen is bound to live state — pair it with the wavy progress indicator under the Hero stat pattern (§5) so bar and number move as one motion. Raw `Text` for a number that can change should be treated as a bug.
- **Lazy-item mutation animator** — the canonical wiring for animating add / move / remove on a lazy list when the change is user-triggered. `rememberLazyItemMutationAnimator(keys)` snapshots the initial set of keys on first non-empty composition; `rememberMutationAnimatedModifier(animator, itemKey)` returns a `Modifier` (carrying `Modifier.animateItem()` plus a `drawWithContent` accent-bar pulse) that the caller applies to the outermost composable of each lazy item. Apply directly — never via an intermediate `Box`, since an extra layout node interferes with lazy-item measurement. Items added or removed after the snapshot fade and reflow; new items also get a brief 20×1 dp accent-bar pulse (§2.3 inline-bar hairline) at their top edge. Suppressed when system animations are disabled. Use it for shelves and chip rows where the user's own action is the "what changed" signal; do not apply it to carousels backed purely by server data (trending, continue-series).

## 5. Patterns

Recurring recipes that compose the primitives above.

- **Editorial section.** Accent bar (optional) → eyebrow → display headline → body (carousel, list, or block). The default way to introduce any region.
- **Hero stat.** A single oversize number in `statHero` or `statLarge`, an editorial suffix beneath in italic body, on-surface-variant. Used for "books read this year", "pages today", and progress sheets. The unit (e.g. "pages", "books") is named in the eyebrow above the number, never trailed next to it — pairing a unit suffix with the oversize numeral fights for horizontal space and breaks once the number grows. The number itself uses single-line autosize down from `statHero` so power-reader counts (six- or seven-digit page totals) still fit the card without reflowing the layout. The numeral is rendered via `AnimatedStatNumber` (§4) so changes tween in place — raw `Text` for a live-bound stat is a bug.
- **Editable hero stat.** Same as hero stat, but the number is a borderless `BasicTextField` styled with `statLarge`. Cursor is in primary colour. Width is computed from character count so the field does not jump as digits are typed.
- **Editorial quote.** Oversized low-alpha `quoteGlyph` (tinted `onSurfaceVariant`) behind an italic headline-medium pull quote, with a primary-coloured byline beneath. Used to surface notes, reviews, or a featured passage. The same glyph treatment also serves as a decorative flourish in empty states (e.g. "no books currently reading"), where the quote text below is replaced by an italic encouragement headline and no byline.
- **Reserved-row card.** Carousel and grid cards reserve vertical space for every optional row they may show, so the row of cards never reflows as data streams in.
- **Tonal grouping.** When two regions need to feel distinct on the same page, step the container shade rather than drawing a divider or adding a card outline.

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
