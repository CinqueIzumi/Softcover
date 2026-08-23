# Design System — Decision rules

## 6. Decision rules

When a new surface is being built, walk this list before reaching for novelty.

- **Building a component rather than a screen?** Read the component contract (§7) first — the signature, the UI-model rules, and where the mapper goes are settled there, and two of the rules are build gates.
- **Need a primary action?** Filled button. One per region.
- **Need an action with a small set of variants?** Split button.
- **Need to edit a single bounded number?** Hero stat field inside a modal sheet.
- **Need to introduce a region of content?** Editorial section (eyebrow + headline).
- **Need to mark a region as elevated relative to its neighbour?** Step the container shade. Do not add a shadow.
- **Need an accent on a piece of text?** Use primary colour on the eyebrow or a leading word. Do not bold or recolour body copy.
- **Need a specific colour?** You need a *role*, not a hex — read it off the scheme, and never assume the app is warm or that the page is near-white: the reader may be in any of five spine colours (§2.1), which retint the paper as well as the ink. A colour that must survive every palette joins the fixed inks in `Color.kt` instead.
- **Need the reader to pick a whole-app look?** Compose `PreviewTileFrame` (§4) — a miniature of the app's own page, not a colour swatch or a radio row.
- **Need a divider?** First try a vertical gap or a tone change. Reach for a hairline divider only when neither will do.
- **Need a custom font weight, size, or italic toggle?** Pick a different role from the editorial scale instead.
- **Need to show progress?** Wavy progress indicator. Always.
- **Need a modal interaction?** `AdaptiveModalSheet` first (a bottom sheet on compact/medium, a centered editorial panel on expanded — §3.5); full-screen second; a bare `Dialog` only for an overlay too transient to earn the editorial header.
- **Laying out a list or grid on a large window?** Scale columns by the width class (§2.7) and cap the total content width (§3.6) — never stretch a fixed-column layout edge-to-edge. Respect a user-chosen density (e.g. the library grid count); add columns or cap width around it, don't override it.
- **Putting long-form body or a form on a large window?** Centre it at the editorial reading width (`cappedContentWidth`, §3.6). Keep hero and cover media full-bleed.
- **Building a root list surface that opens a detail?** Route the open through the shell's `BookDetailPresenter`, not a direct push — it adapts to the list–detail two-pane on expanded (§5) for free.
