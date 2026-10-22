# BookCover

BookCover is the 2:3 book jacket, drawn from loaded art or from an ink monogram when no art exists.

## When to use it

Use it for every book or edition cover. A cover slot is never blank: when there is no art, show the monogram jacket with the title. Never mock a cover as an empty grey box.

## What the consumer provides

- The art, or the title for the monogram fallback. The jacket is always 2:3; set only its width.
- A corner treatment from the variants: `shelf` (library grid, 10px), `rounded` (`radius-lg`), `tile` and `spotlight` (`radius-sm`), and `raised`, `floating`, `jacket`, `stacked-jacket`, `flat` (`radius-xs`). Class names are `sc-cover--<variant>`.
- A loading flag, which shows a flat skeleton field in place of the jacket.

## Do

- Set the monogram title in `editorial-headline-medium` (Fraunces, italic, semibold), centred, in `monogram-cover-foreground` on `monogram-cover-ink`. These two inks are fixed and read the same in every theme.
- Keep the inset hairline at `monogram-cover-foreground` at 24%, inset 6% of the width (2px to 8px, `radius-xxs`).
- Scale the title at 15% of the width, 10px to 28px. Below 64px wide show only the first letter, at 36% of the width.

## Don't

- Don't add a shadow beyond the variant's own; `rounded` and `flat` have none.
- Don't use a theme colour for the monogram. It is a printed jacket.
- Don't stretch or crop the jacket away from 2:3.

## Tokens

`monogram-cover-ink`, `monogram-cover-foreground`, `surface-container-high`, `radius-xxs`, `radius-xs`, `radius-sm`, `radius-md`, `radius-lg`, `space-xs`, `editorial-headline-medium`.
