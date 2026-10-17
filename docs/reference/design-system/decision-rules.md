# Design System — Decision rules

## 6. Decision rules

Extends [fd §10](../../rhaydus/0.3.1/design-system-foundations.md#10-decision-rules-shared-core); walk both before reaching for novelty.

- **Building a component rather than a screen?** Read the [component contract](component-contract.md) (§7) first.
- **Need an action with a small set of variants?** Split button.
- **Need to edit a single bounded number?** Hero stat field inside a modal sheet.
- **Need an accent on a piece of text?** Primary colour on the eyebrow or a leading word. Do not bold or recolour body copy.
- **Need a specific colour?** A role, never a hex: see §2.1.
- **Need the reader to pick a whole-app look?** Use `ThemePreviewTile` / `ColorPalettePreviewTile`: a miniature of the app's own page, not a colour swatch or a radio row.
- **Need a modal interaction?** `:core:component`'s `AdaptiveModalSheet` (§3.5).
- **Laying out a list, grid, long-form body or form on a large window?** See §3.6. Respect a user-chosen density (e.g. the library grid count).
- **Building a root list surface that opens a detail?** Route the open through the shell's `BookDetailPresenter`, not a direct push, so it adapts to the [list–detail two-pane](patterns/cross-cutting.md#list-detail-two-pane).
