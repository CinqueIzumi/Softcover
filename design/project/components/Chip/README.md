# Chip

Chip is a fully rounded label for a filter, a choice, a tag or a status, coloured by its tone and whether it is selected.

## When to use it

Use a tonal chip for a filter, a choice chip for a sort or grouping control, filled or container for a state that is on, outlined and dashed for secondary and add actions, and read-only for a plain tag.

## What the consumer provides

- A label, one tone, and a selected flag.
- Optionally one leading icon (18px) and one trailing slot: icon, disclosure arrow, or dismiss (16px).
- A size: `regular` (`space-sm` by `space-xs` padding) or `compact` (`space-xs` by `space-xxs`).
- An interaction: clickable, disabled (40% opacity), or read-only.

## Do

- Use `secondary-container` with `on-secondary-container` for a selected tonal chip, and `surface-container-high` with `on-surface` for an idle one.
- Use `primary` with `on-primary` for a selected choice chip and for filled; `primary-container` with `on-primary-container` for container.
- Set the label in `label-medium`, semibold where the tone is filled, container, dashed or selected choice.
- Hide a spoiler label behind `spoiler-cover`; reveal it on tap.
- Place an over-media chip on imagery only, over `over-media-scrim` at 40% with `over-media-ink`.

## Don't

- Don't use `error` colours for a chip.
- Don't truncate to nothing: a long label ellipsises on one line.
- Don't use the over-media tone on a plain surface; its text needs the image behind it.

## Tokens

`surface-container-high`, `on-surface`, `secondary-container`, `on-secondary-container`, `primary`, `on-primary`, `primary-container`, `on-primary-container`, `on-surface-variant`, `outline-variant`, `spoiler-cover`, `spoiler-editor-highlight`, `over-media-scrim`, `over-media-ink`, `radius-pill`, `radius-xxs`, `space-xs`, `space-sm`, `label-medium`.
