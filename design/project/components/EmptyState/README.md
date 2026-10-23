# EmptyState

EmptyState explains an empty region in the voice of the app: a title, a line of body copy and, when there is a next step, one button.

## When to use it

Use the centered form when the region is empty on its own, with a call to action where the user can fix it. Use the inline form for an empty list inside a section, where the content below remains.

## What the consumer provides

- A title, an optional body, and for centered an optional large filled PillButton.
- Copy in the app's editorial voice: short, specific, no apology.

## Do

- Open the centered form with a quote mark in `editorial-quote-glyph`, `on-surface-variant` at 30% (15% in dark and dim), then `space-sm`, the title in `editorial-headline-medium`, `space-sm`, the body in `editorial-body-large` in `on-surface-variant`, and `space-xl` before the button.
- Set the inline form left-aligned: title `editorial-headline-small`, `space-xxs`, body `editorial-body`.
- Write copy like "Between books" and "Nothing on the shelf".

## Don't

- Don't show a sad face.
- Don't add a second button.
- Don't use the centered form inside a short section.

## Tokens

`on-surface`, `on-surface-variant`, `primary`, `on-primary`, `space-xxs`, `space-sm`, `space-xl`, `editorial-quote-glyph`, `editorial-headline-medium`, `editorial-headline-small`, `editorial-body-large`, `editorial-body`.
