# SectionHeader

SectionHeader opens a section of a page: a bar and an eyebrow, optionally followed by an italic headline and a line of description, or as a quieter inline or label register.

## When to use it

Use the section register to start each major block on a page, the inline register for a list within a section (with an error tone for a retry line), and the label register for a short caption over a group.

## What the consumer provides

- An eyebrow, shown in capitals.
- For a section: an optional headline, with an optional emphasised word, and an optional description.
- For inline and label: a tone, `primary` (default for inline), `error`, or muted `on-surface-variant` (default for label).
- Optionally trailing actions at the end of the row.

## Do

- Pair the 32 by 4 `primary` bar (`radius-xxs`) with `editorial-eyebrow`, `space-sm` apart.
- Set the headline in `editorial-headline-medium` in `on-surface`, `space-xs` below the eyebrow; colour one emphasised word `primary`.
- Set the description in `editorial-body` in `on-surface-variant`.
- For inline use the 20 by 1 bar with `editorial-eyebrow-small`, `space-xs` apart.
- Leave `space-section` between two section headers.

## Don't

- Don't stack two eyebrow and headline pairs without the section gap.
- Don't use `primary` text for more than one emphasised word.
- Don't use the section register inside a card.

## Tokens

`primary`, `error`, `on-surface`, `on-surface-variant`, `space-xs`, `space-sm`, `space-section`, `radius-xxs`, `editorial-eyebrow`, `editorial-eyebrow-small`, `editorial-headline-medium`, `editorial-body`.
