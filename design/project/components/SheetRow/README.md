# SheetRow

SheetRow is one single-choice row in a sheet: a rule, an optional icon, an italic label, an optional count and a check when selected.

## When to use it

Use it for a list of exclusive options in a sheet, such as sort order or a shelf to move a book to. Group several rows under one title; a selected row carries the check and the accent colour.

## What the consumer provides

- A label (up to two lines), a selected flag, an optional leading icon and an optional count with its spoken form (`12`, "12 books").

## Do

- Rule each row with a 1px `outline-variant` line, with `space-sm` above and below and between parts.
- Set the label in `editorial-title-large` italic; `on-surface` when idle, `primary` when selected, and tint the icon the same.
- Set the count in `editorial-eyebrow-small` in `on-surface-variant`.
- Reserve the 18px check slot in every row so labels stay aligned.

## Don't

- Don't use a SheetRow for multi-select; it is a radio.
- Don't add a second selection cue such as a filled background.
- Don't put a count on only some rows of a group.

## Tokens

`on-surface`, `primary`, `on-surface-variant`, `outline-variant`, `space-sm`, `editorial-title-large`, `editorial-eyebrow-small`.
