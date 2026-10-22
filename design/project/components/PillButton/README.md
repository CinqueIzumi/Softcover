# PillButton

PillButton is the app's compact pill action in five tones and two heights, with a label or a single icon.

## When to use it

Use filled for the one primary action in a region, tonal and accent for secondary actions, outlined for an external or alternative action, and quiet for a toolbar-style control such as Filter or Sort. Use the large height for a call to action in an empty state or a sheet footer.

## What the consumer provides

- A label with an optional leading icon, or an icon alone with an accessible description.
- A tone and a size: regular 38px, large 44px; icon-only buttons are square at that height.
- An optional 6px `primary` indicator dot, for an active filter.
- Whether it is enabled; disabled is 40% opacity.

## Do

- Use `primary` with `on-primary` for filled, `surface-container-high` with `on-surface` for tonal, `primary` text on `surface-container-high` for accent, `on-surface-variant` on `surface-container` for quiet.
- Give outlined a 1px `outline-variant` border and a `primary` icon.
- Set the label in `label-medium`, semibold, with `space-xs` between icon and label.

## Don't

- Don't place two filled buttons side by side.
- Don't use a PillButton for navigation between screens.
- Don't colour a button with `error`; destructive confirmation lives in a dialog.

## Tokens

`primary`, `on-primary`, `surface-container-high`, `surface-container`, `on-surface`, `on-surface-variant`, `outline-variant`, `radius-pill`, `space-xs`, `space-sm`, `label-medium`.
