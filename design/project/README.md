## Content fundamentals

Softcover is a book tracker that reads like a well-edited magazine. The character comes from the type and the layout, not from the wording.

- Write short, specific sentences in plain words. Address the reader as "you". Never say "we".
- Name the content in a headline: "Up next in your series", "Recent favourites". Say what the reader sees or what happens next in a gloss or caption.
- Describe only behaviour the product has. A gloss that promises something the app does not do is a bug.
- Refer to a setting, theme or button by the label on screen, never by a nickname or metaphor.
- State errors and status plainly: what failed, what to do. Real copy: "Couldn't save your progress. Try again from the app." and "You're offline. Progress changes are saved and will sync when you reconnect." and "This screen needs internet to load. Reconnect to try again."
- Use sentence case for every headline, label and button. Write an eyebrow in sentence case too: the component renders it in capitals.
- Use British spelling: colour, favourite, organise.
- Put digits in data copy ("between 4 and 5"). Use the ellipsis character for work in progress ("Getting your shelves ready…"), never three dots.
- End a gloss, caption, body line or snackbar with a full stop. Give headlines, labels and buttons none.
- Do not use emoji, exclamation marks, em dashes or en dashes. Use a full stop, a colon or a comma.
- Cut the generated tells: personification ("Your access token lives here."), literary flourish, forced metaphor, cute labels ("Volumes" for "Books"), tidy triplets, cheerleading ("Your next favourite is still ahead of you."). Say "Books you rate highly show up here."

## Visual foundations

### Colour

The look is warm paper and clay. Use a colour by its role token, never by a hex value. The default palette is Softcover; light, dark and dim are the three themes, and every token below has a value in each. Dim is one standalone warm scheme that ignores the palette. The four other palettes (Vellum, Ink, Foxed, Sea) are in the Palettes section; each retints the paper as well as the accents.

Grounds, quietest to strongest:
- `background` and `surface` are the page. They are the same value.
- `surface-container-lowest` holds modal sheets and overlays.
- `surface-container-low`, `surface-container`, `surface-container-high` and `surface-container-highest` step up for cards, tiles, idle chips and focused cards or inputs.
- `surface-variant` is a tinted region that is not a card.
- `inverse-surface` is the fill of a snackbar or tooltip.

Text and icon inks, each with the grounds it reads on:
- Set body copy in `on-surface`. It reads on `background`, `surface`, every `surface-container-*` and `surface-variant` in light and dark. In dim it reads on all of them too (5.5:1 on `surface-variant`).
- Set demoted copy (metadata, captions, inactive labels) in `on-surface-variant`. It reads on `background`, `surface` and every `surface-container-*` in all three themes. Do not put it on `surface-variant` in dim (4.1:1): use `on-surface` there. It is 4.6:1 on `surface-container-highest` in dim, which is the floor.
- Use `primary` for the one accent: eyebrows, section marks, progress, active toggles and filters, a leading accent word. It reads on `background`, `surface`, every `surface-container-*` and `surface-variant` in all three themes (5.0:1 at the lowest, light on `surface-container-highest`).
- Fill at most one hero card per scrolling region with `primary`, and put `on-primary` on it (6.4:1 light, 7.7:1 dark and dim). Demote text inside it with `on-primary` at about 75% alpha, and its divider at about 25%. Never use `primary` as any other background. Cards built around imagery keep a `surface-container-*` fill.
- Use `secondary` for quieter supporting emphasis and `tertiary` for rare, distinct emphasis. Both read on the same grounds as `primary`.
- Use `error` for destructive actions and validation messages only. It reads on `background`, `surface` and the containers in all themes, except dim on `surface-container-highest` (4.5:1, at the floor) and on `surface-variant` (4.0:1, which misses: do not use it there). In dark, `error` on `surface-variant` is 4.1:1, which also misses.
- Pair each container with its partner: `on-primary-container` on `primary-container`, `on-secondary-container` on `secondary-container` (the selected state of chips and facet toggles), `on-tertiary-container` on `tertiary-container`, `on-error-container` on `error-container` (banners, destructive confirmations). Every pair is 5.5:1 or better in all themes. `on-secondary`, `on-tertiary` and `on-error` sit on their solid fills at 5.7:1 or better.
- Put `inverse-on-surface` on `inverse-surface` (7.7:1 or better in all themes). Use `inverse-primary` only for small attention markers such as a notification dot; it is 3.8:1 on `inverse-surface` in dim, which is enough for a marker and not for text.
- Borders: draw the rare bordered control and strong dividers in `outline`. It holds 3:1 against `background`, `surface` and the containers in light and dark, and in dim against `background`, `surface`, `surface-container-lowest`, `surface-container-low` and `surface-container` (3.1:1 at the lowest). Dim `outline` on `surface-container-high` (2.8:1) and `surface-container-highest` (2.5:1) misses 3:1: do not rely on it as the only edge there.
- Draw hairline dividers in `outline-variant`. It is decorative and sits near 1.5:1 to 2:1 by design, so it never carries meaning alone. Prefer whitespace or a tone change to a dense rule.
- Veil modals with `scrim`. Do not paint with `surface-tint`.
- Use `surface-bright` for chrome that stays bright in dark themes and `surface-dim` for recessed regions in light themes.

Fixed inks read the same in every theme and palette: `rating-gold` for rating stars only; `over-media-scrim` and `over-media-ink` for chips, glass and buttons over imagery; `mood-ink-cosy-background`, `mood-ink-cosy-foreground`, `mood-ink-dread-background`, `mood-ink-dread-foreground`, `mood-ink-dread-eyebrow`, `mood-ink-heart-wrench-background` and `mood-ink-heart-wrench-foreground` for the mood tiles; `monogram-cover-ink` and `monogram-cover-foreground` for a book with no cover; `spoiler-cover` and `spoiler-editor-highlight` for spoiler runs only. Do not use the `*-fixed` and `*-fixed-dim` tokens: they are Material defaults, not brand colours.

Never recolour body copy to add emphasis.

### Type

Two families. Fraunces (the `display` family) is the editorial serif with true italics. Inter (the `body` family) is the chrome sans, roman only. Use `font-feature-settings: "tnum"` on the two stat styles.

Editorial styles, in use order:
- `editorial-eyebrow`: the all-caps section label over a headline, Inter semibold, wide tracking, in `primary`. Pair it with a 32 by 4 bar in `primary`.
- `editorial-eyebrow-small`: the same label packed inside a card or row. Pair it with a 20 by 1 bar or none. As a plain sub-label in a section already introduced, set it in `on-surface-variant` with no bar.
- `editorial-page-title`: the screen's primary title inside the scrolling page, Fraunces roman semibold at 30px. The top bar slot of a pushed screen uses `title-large` instead.
- `editorial-display`: the section headline that follows an eyebrow, Fraunces italic semibold at 45px, in `on-surface`.
- `editorial-headline-medium` and `editorial-headline-small`: subsection and modal headlines, italic semibold.
- `editorial-title-large`, `editorial-title-medium` and `editorial-title-small`: card titles, list item titles and grouped row headers. Inter roman semibold.
- `editorial-body-large`, `editorial-body` and `editorial-body-small`: running prose, descriptions and captions, Fraunces italic. Use `editorial-body` by default.
- `editorial-review`: community review prose, Fraunces roman, so a reader's words are never styled as editorial copy.
- `editorial-stat-hero`: the single largest number on a screen (72px). `editorial-stat-large`: hero numbers and input fields styled as stats (45px).
- `editorial-quote-glyph`: an oversized quotation mark behind a quote or as an empty-state flourish. Draw it in `on-surface-variant` at low alpha.

Italic belongs to display, headlines, body prose and stats. Page titles, card and list titles, and chrome are roman. A chrome surface that needs italic belongs in the editorial scale.

The Material styles (`title-large` through `label-small`, plus `body-large`, `body-medium`, `body-small`) are for Material component internals: `label-large` for button labels, `label-medium` for chip and tab labels, `label-small` for badges. Reach for an editorial style first. The `display-*` and `headline-*` Material styles are a fallback.

Text colour follows the ground table above. A text 24px or larger needs 3:1; below that, 4.5:1.

### Spacing

Spacing is rhythmic. Read it from the scale: `space-xxxs` (2px, the hairline gap), `space-xxs` (4px) and `space-xs` (8px) for tight inline gaps such as icon to label, `space-sm` (12px) and `space-md` (16px) for padding within a component and the gap from a section header to its body, `space-lg` (20px) between a hero card's image and its text, `space-xl` (24px) between blocks inside a sheet, `space-xxl` (28px) between blocks inside a hero region, `space-xxxl` (32px) from a masthead to its first section, `space-x4l` (40px) and `space-x5l` (48px) between major sections.

- Pad every page's horizontal edges with `space-gutter` (24px), on every platform.
- Put `space-section` (40px) between major sections. Two adjacent eyebrow and headline pairs without it is a layout bug.
- Sizes and strokes stay literal pixels; only gaps and padding read a `space-*` token.

### Radius

Use `radius-xxs` (2px), `radius-xs` (4px), `radius-sm` (8px), `radius-md` (12px), `radius-lg` (16px), `radius-xl` (20px) and `radius-xxl` (28px). Snap an off-scale radius to the nearest step. Use `radius-pill` for chips and buttons with fully rounded ends. A true circle stays a circle. Give book covers a subtle radius, `radius-xs` or `radius-sm`.

### Borders, shadows and elevation

Softcover is flat. Express grouping with whitespace and hairlines, not with a shadow: grouped rows sit flat on `background` (step 5 under Composing a screen), and `surface-container` cards are for book and hero content only. Separate with whitespace first, a hairline in `outline-variant` second, a bordered control in `outline` rarely. The one shadow exception is the book cover, which carries a slight shadow. On a wide window a modal sheet becomes a centred `surface-container-lowest` panel, up to 420px wide, no border, `radius-xxl` corners and a 6px shadow over a `scrim`. Do not use coloured left-border cards.

### Imagery

Book covers are the imagery. Render covers at book proportions with the subtle radius and slight shadow, and fall back to a monogram jacket in `monogram-cover-ink`, never a blank box. Text over imagery uses `over-media-ink` on `over-media-scrim`. Illustrations are in the Illustrations asset group.

### Motion and states

- Make a value change spring, critically damped and slow, for stat numbers and chart sizes.
- Press feedback is a small scale-down on the pressed element, with a hover wash on desktop and a hand cursor. There is no ripple.
- A tab swap is a 200ms cross-fade with a 12px upward drift on the incoming tab. A push fades the incoming screen in while scaling up from 0.92; a pop reverses it.
- A rejected save shakes its card or row (6px, 3 cycles), turns the eyebrow to `error`, and reads "Couldn't save · tap to retry" in capitals.
- Show progress with the wavy indicator. Show loading with a shimmer over the leaf, not a spinner.
- Respect reduced motion: values snap and decorative effects (burst, shimmer, sway) are skipped.
- Selected chips and facet toggles use `secondary-container`; idle chips sit on `surface-container-high`. An active toggle or filter uses `primary`.

### Layout

- Root tabs carry an in-page `editorial-page-title`, not a chrome title. A pushed sub-screen uses a top bar with a `title-large` title and a back button.
- A section is an `editorial-eyebrow` in `primary` with its bar, an `editorial-display` headline in `on-surface`, a body, then a `space-section` gap.
- Show collections of books as horizontal carousels with 24px content padding, so a clipped card never butts flush.
- A card is, top to bottom: cover, title (`editorial-title-medium` or `editorial-title-small`, two lines at most), an optional metadata strip in `on-surface-variant`, an optional trailing accent in `primary`. A vertical list lays the same anatomy out horizontally, cover at the leading edge.
- Navigation chrome follows the window: a bottom bar on compact, a rail on medium, a sidebar on expanded. Reserve space for the bottom bar on any surface it overlays.
- On a wide window cap reading columns at a readable width with the gutter inside the cap; keep full-bleed media outside it.
- Modal sheets open with `editorial-eyebrow`, `editorial-display` or `editorial-headline-medium`, then `editorial-body`.

## Iconography

Icons are the `SoftcoverIcon` set, shipped as SVGs under `assets/Icons/`: a Material-style outline set drawn on a 960 by 960 box and rendered at 24px. Each file is single-ink and takes its colour from `currentColor`, so set the ink with CSS `color`. Never recolour a path.

- Ink an icon in `on-surface` by default, `on-surface-variant` when demoted, `primary` when active, and `on-primary` inside a primary-filled hero. Icons need 3:1 against their ground, which every pairing above meets in light and dark. In dim, `on-surface-variant` on `surface-variant` misses it for text but passes at 4.1:1 for icons.
- Use `star-filled.svg` for ratings, inked in `rating-gold`, and nothing else in that colour.
- Pick by name from `assets/Icons/`: `search.svg`, `add.svg`, `close.svg`, `check.svg`, `settings.svg`, `edit.svg`, `delete.svg`, `share.svg`, `filter-list.svg`, `sort.svg`, `bookmark.svg`, `shelf.svg`, `menu-book.svg`, `library-books.svg` and the rest. Do not invent an icon or pull in another set.
- Give an icon-only control a tooltip or an accessible label. Do not use emoji or text glyphs as icons.
- Empty states and sign-in use the SVGs under `assets/Illustrations/`, with a `-dark` variant for dark themes.

## Screens

Each page below describes one existing screen family: its header register, group spacing and row anatomy.

- [Settings](screens/settings.md): the Settings menu, its rows, Appearance, About and Library tabs.
- [Explore and Profile](screens/explore-profile.md): the Explore tab and the Profile screen.
- [Book detail](screens/book-detail.md): the book page and its sections.
- [Library](screens/library.md): the Library tab, its shelves and filters.
- [Reading](screens/reading.md): the Reading tab and its featured hero.
- [Widgets](screens/widgets.md): the home-screen widgets and their settings.

## Composing a screen

1. For an existing screen, start from its page under Screens and match its header register, group spacing and row anatomy before drawing.
2. Pick the page canvas: `background`, with copy in `on-surface`.
3. Title the page with `editorial-page-title` if it is a root tab, or a `title-large` top bar if it is pushed.
4. Open each section with an `editorial-eyebrow` in `primary` and its 32 by 4 bar, then an `editorial-display` headline, then `editorial-body`. Separate sections with `space-section`. Use `editorial-eyebrow-small` with a 20 by 1 bar only inside a card or hero.
5. Sit grouped rows (settings-style menus and lists) flat on `background`, a hairline in `outline-variant` above each row, each row led by a 42px `surface-container-high` icon tile with the glyph in `primary`. Use `surface-container` cards for book and hero content only. Step to `surface-container-high` or `surface-container-highest` to express focus, never to a shadow.
6. Pad the page with `space-gutter`. Gaps inside a component are `space-sm` or `space-md`.
7. Give an accent to text by colouring the eyebrow or a leading word in `primary`. Do not bold or recolour body copy.
8. Use one filled button per region. Offer a small set of variants as a split button.
9. Edit a single bounded number as a hero stat field in a modal sheet, in `editorial-stat-large`. Never use a bordered field.
10. Let the reader choose a whole-app look with a miniature of the app's own page, not a colour swatch or a radio row.
11. Show a modal interaction as a sheet, which becomes a centred panel on a wide window.
12. Keep the single primary-filled stat hero to one per scrolling region.
13. Check every text colour against its ground in the table above, in all three themes, before shipping.
