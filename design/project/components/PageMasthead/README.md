# PageMasthead

PageMasthead is the page's own title block: an optional eyebrow, the title, and a subtitle or a live count, with room for actions at the end.

## When to use it

Use it once at the top of a scrolling page, in the regular size for a primary screen and the compact size for a secondary screen or a place where the top bar already carries context.

## What the consumer provides

- A title (unknown while loading), an optional eyebrow, and a subtitle: text, or counts such as `128 titles · 34,000 pages`.
- A size, regular or compact, and a loading flag that masks the subtitle only.
- Optional actions, laid out at the end of the row `space-xxs` apart.

## Do

- Set the regular title in `editorial-page-title` and the compact title in `editorial-headline-medium`, `on-surface`, on one line.
- Set the eyebrow in `editorial-eyebrow`, `primary`, with `space-xxs` before the title.
- Set the subtitle in `editorial-body` (regular) or `editorial-body-small` (compact), `on-surface-variant`, at most 300px wide.
- Leave `space-xxxl` between the masthead and the first section.

## Don't

- Don't repeat the page title in the top bar.
- Don't wrap the title; it ellipsises.
- Don't put a second masthead on one page.

## Tokens

`primary`, `on-surface`, `on-surface-variant`, `space-xxs`, `space-xs`, `space-xxxl`, `editorial-eyebrow`, `editorial-page-title`, `editorial-headline-medium`, `editorial-body`, `editorial-body-small`.
