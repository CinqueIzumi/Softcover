# Components — Sheet scaffold

### Sheet scaffold

`SheetScaffold` (`core:component/sheet`) is the chrome every modal sheet shares: an optional dismiss label, the editorial header, the sheet's body, and an optional footer. A sheet's body keeps its content; the scaffold owns the scroll, the footer, and the close.

- **Layout** — `SheetLayout.Scrolling` scrolls the whole sheet with the footer at the end. `Pinned` fixes the dismiss label and header, scrolls the body, and sticks the footer under a divider. `PinnedLazy` is `Pinned` for a body that owns a lazy list. Bodies never add their own scroll or a bottom spacer.
- **Dismiss label** — `dismissLabel` renders a right-aligned text button above the header; tapping it runs the animated hide.
- **Footer** — `SheetFooterUiModel.Stacked` (primary full width, secondary beneath, at least one present) or `Inline` (secondary left, primary fills the row, both present). A button with `dismissesSheet` hides the sheet after emitting its click.
- **Busy** — `busy` puts a progress indicator on the primary button, disables the footer and makes the sheet non-dismissible; the scaffold, not the model, disables the secondary button. `dismissible = false` also stops the dismiss label and `dismissesSheet` buttons from hiding the sheet.
- **Closing from state** — an action sets `closeRequested`; the scaffold hides the sheet, then emits `Dismissed`, and the host clears its show state there. `closeRequested` hides the sheet regardless of `dismissible`. Once a hide starts the chrome, scrim tap and back press are disabled, so `Dismissed` emits once. A swipe-down still closes a non-dismissible bottom sheet.
- **Semantics** — the headline is the heading and the sheet's pane title; a busy primary button reports a state description.

### Sheet rows

`SheetRowGroup` renders hairline-divided single-choice `SheetRow`s (italic label, optional leading icon, count, check on the selection). `SheetRowGroupUiModel.dismissesSheetOnSelect` hides the sheet after `Selected`. A row's `count` pairs with `countDescription` so a screen reader hears "12 titles" rather than a bare number.
