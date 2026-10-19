# Patterns — Editorial

### Editorial section

Accent bar (optional) → eyebrow → display headline → body (carousel, list, or block). The default way to
introduce any region.

### Hero stat

- One oversize number in `statHero` or `statLarge`, an italic `onSurfaceVariant` editorial suffix beneath.
  Used for "books read this year", "pages today" and the progress sheets.
- The unit ("pages", "books") is named in the eyebrow above the number, never trailed beside it.
- A live-bound numeral renders through `StatNumber`, never raw `Text`.

### Editable hero stat

- The hero stat for a number the reader edits: the foundation's `HeroStatNumberField` paired with an
  `EditorialSuffix` caption ("of 195 pages"), both from `nl.rhaydus.designsystem.editorial.component`.
- Used by the Update progress sheet and Focus Mode; never a hand-rolled boxed field.

### Editorial quote

- Featured form: an oversized low-alpha `quoteGlyph` (`onSurfaceVariant`) behind an italic `headlineMedium`
  pull quote, with a `primary` byline beneath. Surfaces a review or a featured passage.
- List form (`QuoteCard`, the reader's saved quotes): the editorial `bodyLarge` passage (regular italic) under a
  48sp mark.
- Its empty-state form is `EmptyState`: an italic encouragement headline in the quote's place, no byline.

### Standfirst

- A book's own `headline`, opening the book-detail About section directly under its `SectionHeader`, in
  the editorial `bodyLarge` role (italic, `onSurface`), ≈16dp above the drop-cap description.
- Plain text, never run through `htmlToAnnotatedString`. Omitted when the book has no headline; with a
  headline and no description it stands alone and the "No description…" fallback is dropped.

### Prose toggle

- A sentence whose words are the control: `SegmentedControl` with `SegmentedControlVariant.Prose`.
- Used by the create-list sheet's privacy sentence ("This shelf is *public* / *private*.").

### Rendered markdown document

- A flat, source-order block list (heading, paragraph, list item, blockquote, thematic break) rendered as
  an editorial spread, never raw markdown.
- A `"3.2.0: Title"` heading becomes a `SectionHeader` eyebrow + headline; list items hang-indent; a
  thematic break is a hairline; a blockquote is an accent-bar aside; links are `LinkAnnotation.Url` runs.
- Rendered by the `:core:component` `MarkdownDocument`. Used by Settings → About → Roadmap (`ROADMAP.md`)
  and What's new (one per `CHANGELOG.md` entry, under its version header); reuse it for the next
  markdown-shaped document rather than a second renderer.
