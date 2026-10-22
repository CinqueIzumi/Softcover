# ColumnChart

ColumnChart is a row of equal-width columns, scaled by a fraction, with an optional value above and a label below each.

## When to use it

Use it to compare a handful of values along one axis: books per year, a rating distribution. For parts of a whole use a proportion chart, and for ranked names use ranked bars.

## What the consumer provides

- For each column: a fraction from 0 to 1, a tone by rank, an optional label and value, and a description for assistive technology.
- Whether values are shown above the columns.
- A loading flag; bars hold at a mid height and skeleton blocks stand in for text.

## Do

- Draw the tallest column in the lead tone, `primary`; step the others down as `primary` at 82%, 64%, 48%, 34%.
- Size each column 12px plus 84px times its fraction, 20px wide at most, in a 96px plot; a zero column is a 3px stub. Round corners at `radius-xxs`.
- Leave `space-xs` between columns, values and labels.
- Set values and labels in `editorial-eyebrow-small`: `on-surface-variant`, and `primary` bold for the lead value.

## Don't

- Don't rely on tint alone; the lighter tones are under 3:1 against the page, so always give a label or value.
- Don't add gridlines or axes.

## Tokens

`primary`, `on-surface-variant`, `radius-xxs`, `space-xs`, `editorial-eyebrow-small`.
