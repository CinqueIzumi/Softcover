# BookCard

BookCard is the one book or series card: a bare cover, a cover over its text, or a row in three sizes, used by every library, explore, reading, profile and edition surface.

## When to use it

Use the cover-only form for a grid of jackets, the column form for a cover with its title beneath, and the row form for lists, edition pickers and reading lists. Pick one form per list.

## What the consumer provides

- A `BookCover` in the `raised` variant, plus an optional badge laid over its top-end corner.
- Text: an optional eyebrow, a title, an optional byline, supporting lines, and an optional progress value.
- For rows: a size (`compact` 40px lead, `standard` 56px, `large` 74px), an optional trailing control (badge, bookmark toggle, set-progress pill), and an optional deadline line.
- Selection: `multiple` dims unselected covers to 55% and shows a mark; `single` shows the ring and a mark on the chosen card only.

## Do

- Set column titles in `editorial-title-small` over two lines, row titles in `editorial-title-medium` (`editorial-title-large` for the large row), bylines in `editorial-body-small` in `on-surface-variant`.
- Set the eyebrow in `editorial-eyebrow-small`: `primary`, or `error` for a failure.
- Rule each row with a 1px `outline-variant` line at the top, with `space-sm` above and below the content.
- Show a selected cover at 92% scale with a 3px `primary` ring over a 16% `primary` wash.
- Reserve the 12px progress row where a card may gain progress, so the grid does not move.

## Don't

- Don't put a shadow or card background behind a column; the cover and text sit on the page.
- Don't mix row sizes in one list.
- Don't put more than one badge on a cover.

## Tokens

`primary`, `on-primary`, `primary-container`, `error`, `error-container`, `on-error-container`, `on-surface`, `on-surface-variant`, `outline-variant`, `surface`, `surface-container-high`, `rating-gold`, `space-xxxs`, `space-xs`, `space-sm`, `radius-xs`, `editorial-eyebrow-small`, `editorial-title-small`, `editorial-title-medium`, `editorial-title-large`, `editorial-body-small`.
