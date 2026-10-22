# StatTile

StatTile is a rounded tile that pairs a small eyebrow with a figure, in a hero, a standard and a compact form.

## When to use it

Use the hero for the single headline figure of a region, the standard for a grid of statistics with a caption, and the compact for a short one-line figure with a unit.

## What the consumer provides

- An eyebrow, a figure (see StatNumber) and, per form, a caption (hero, standard) or a unit (compact).
- The tile size for standard and compact; the hero fills its row.
- A loading flag that masks the figure.

## Do

- Fill the hero with `primary`: eyebrow in `editorial-eyebrow` and caption in `editorial-body`, both `on-primary` at 75%, the figure in `editorial-stat-hero` in `on-primary`, a 1px `on-primary` rule at 25% between them. Pad it `space-xl`.
- Fill the standard with `surface-container-high`, padded `space-md`: eyebrow `editorial-eyebrow-small` in `primary`, figure `editorial-stat-large`, caption `editorial-body-small` in `on-surface-variant`.
- Fill the compact with `surface-container`: `editorial-eyebrow-small` in `primary`, figure in `editorial-headline-medium`, unit in `editorial-body`.
- Round all three at `radius-xl`.

## Don't

- Don't use more than one hero tile per scrolling region; `primary` is never any other background.
- Don't mix the three forms in one row.
- Don't put a shadow on a tile.
- Don't set long copy in the demoted text; on-primary at 75% on `primary` measures 4.3 to 4.4:1 across the themes, just under 4.5:1.

## Tokens

`primary`, `on-primary`, `surface-container-high`, `surface-container`, `on-surface-variant`, `radius-xl`, `space-xs`, `space-sm`, `space-md`, `space-xl`, `editorial-eyebrow`, `editorial-eyebrow-small`, `editorial-stat-hero`, `editorial-stat-large`, `editorial-headline-medium`, `editorial-body`, `editorial-body-small`.
