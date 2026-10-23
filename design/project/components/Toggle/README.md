# Toggle

Toggle is an icon button with an on and an off state, in a filled tile or a bare icon.

## When to use it

Use the filled toggle for a save or bookmark action beside a book, and the bare toggle for show and hide in a list of settings. Give it a glyph for each state and a description that names the action the next tap performs.

## What the consumer provides

- A checked flag, an on glyph, an off glyph and a description for assistive technology.
- A variant: filled (44px tile, `radius-md`, 24px glyph) or bare (38px, round, 20px glyph).
- Whether it is enabled; disabled is 40% opacity.

## Do

- Fill the checked filled toggle with `primary` and an `on-primary` glyph; leave the unchecked one on `surface-container-high` with a `primary` glyph.
- Draw the bare toggle with no fill: `primary` when on, `outline` when off.
- Swap to the matching glyph on change, not only the colour.

## Don't

- Don't use a Toggle for a setting that needs a label; use a toggle row.
- Don't rely on colour alone; the off and on glyphs must differ.
- Don't put a filled toggle inside a filled tile.

## Tokens

`primary`, `on-primary`, `surface-container-high`, `outline`, `radius-md`, `radius-pill`.
