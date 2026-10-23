# StatNumber

StatNumber is a figure set in tabular numerals, in one of eight sizes from a 72px hero down to an inline body count.

## When to use it

Use it for any number the user reads as a statistic: totals, averages, durations, counts inside a sentence. For a number inside a tile use the tile's own size.

## What the consumer provides

- A value and a format: grouped (`12,481`), plain, one decimal (`4.2`) or duration (`1h 30m`).
- A size: hero, stat large (standard), average (52px), headline, body strong, body, body small or label.
- An optional prefix, and a fixed or singular/plural suffix (`1 day running`, `3 days running`).
- A loading flag; the number is masked at the width of its stand-in.

## Do

- Set hero in `editorial-stat-hero` (72px), standard in `editorial-stat-large` (45px), average in `editorial-stat-large` at 52px, headline in `editorial-headline-small`, body in `editorial-body`, body small in `editorial-body-small`.
- Keep tabular numerals so a changing number does not shift its neighbours.
- Let the figure inherit its colour from the surface it sits on.

## Don't

- Don't use more than one hero number on a screen.
- Don't set a figure in the display face at body size; use `editorial-body`.
- Don't hard-code the plural; give both forms.

## Tokens

`editorial-stat-hero`, `editorial-stat-large`, `editorial-headline-small`, `editorial-body`, `editorial-body-small`, `body-medium`, `label-small`, `on-surface`, `on-surface-variant`.
