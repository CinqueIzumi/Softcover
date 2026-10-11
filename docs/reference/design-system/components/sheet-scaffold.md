# Components — Sheet scaffold

### Sheet scaffold

`SheetScaffold` (`core:component/sheet`) is the chrome every modal sheet shares: an optional dismiss label, the editorial header, the sheet's body, and an optional footer. `header` is nullable: a null header renders none, for a body that carries its own title. A sheet's body keeps its content; the scaffold owns the scroll, the footer, and the close.

- **Layout** — `SheetLayout.Scrolling` scrolls the whole sheet with the footer at the end. `Pinned` fixes the dismiss label and header, scrolls the body, and sticks the footer under a divider. `PinnedLazy` is `Pinned` for a body that owns a lazy list. Bodies never add their own scroll or a bottom spacer.
- **Dismiss label** — `dismissLabel` renders a right-aligned text button above the header; tapping it runs the animated hide.
- **Footer** — `SheetFooterUiModel.Stacked` (primary full width, secondary beneath, at least one present) or `Inline` (secondary left, primary fills the row, both present). A button, or the header `Action`, with `dismissesSheet` hides the sheet after emitting its click. `SheetFooterButton.emphasis` is `SheetFooterEmphasis.Default` (filled for the primary, text for the secondary) or `Outlined`, which draws outlined whatever the button's role; `icon` leads the label.
- **Busy** — a footer button with `running` fades in a progress indicator (trailing on a full-width button; on the inline secondary it replaces the label at the same width). While any button runs, the sheet is `busy`: the footer is disabled and the sheet is non-dismissible. `dismissible = false` also stops the dismiss label and `dismissesSheet` buttons from hiding the sheet.
- **Closing from state** — the host asks for the hide through the sheet model's `closeRequested` (`SheetModel.requestingClose()`, which a scaffold-backed model delegates to its `SheetScaffoldUiModel`); the scaffold hides the sheet, then emits `Dismissed`, and the host moves its `SheetSession` to `Closed` there. A body closes the sheet through `LocalModalSheetDismiss`, which the scaffold routes through the same once-only hide. `closeRequested` hides the sheet regardless of `dismissible`. Once any close path starts (scrim tap, back, swipe or a hide) the footer, header action and all body input are disabled, so `Dismissed` emits once. Scrim, back and swipe are refused while the sheet is non-dismissible or busy. One snackbar sits below the bottom sheet's content and under the panel, so it never covers the footer.
- **Semantics** — the headline is the heading and the sheet's pane title; a running button reports a state description.

### Sheet session

`SheetModel<Self>` is the contract a sheet's UI model meets: `closeRequested` and `requestingClose()`. `SheetSession<Model, Pending>` holds one sheet's lifetime on the host's UiState: `Closed`, or `Open(model, pending)`.

- **Transitions** — `open(model)` opens on a model, replacing a session that is still closing. `openOrUpdate(model)` opens when closed or closing and otherwise swaps the open model. `withModel { }` swaps an open model and keeps the `pending` intent; a close already requested survives the rebuilt model. `requestClose(pending)` asks the sheet to hide, and a null `pending` keeps one already recorded. `dismissed()` returns the recorded `pending`, which the host runs after the `Dismissed` event moved the session to `Closed`, so a follow-up such as leaving the screen or logging out happens only once the sheet has left.
- **Host pattern** — the open action sets a request input on UiState (a flag or an id) and does nothing else. One collector per sheet builds the model from that input and the data it shows, and is the only writer of the session: it calls `openOrUpdate` while the input is set and `requestClose()` otherwise. It re-reads the input inside its `setState` lambda, so a snapshot taken while requested cannot reopen a sheet an action has since closed. An action that closes a collector-owned sheet clears the request input and leaves the hide to the collector; one that must also record a `pending` intent calls `requestClose(pending)` in the same `setState`.
- **Render** — the screen top renders from `session.modelOrNull`, sends `Dismissed` to an action that sets the session to `Closed`, and never copies state into the model at render time.

### Sheet action rows

`SheetActionRow` is a plain tappable menu row (icon, label, description) driven by `SheetActionRowUiModel`. It emits `SheetRowEvent.Selected(key)` and holds no selection state, so a sheet of choices that act at once, such as the continue-series menu, shares it instead of drawing its own rows.

### Sheet rows

`SheetRowGroup` renders hairline-divided single-choice rows (italic label, optional leading icon, count, check on the selection) and runs the sheet's animated dismiss after `Selected` unless `dismissesSheet` is false. A row's `count` pairs with `countDescription` so a screen reader hears "12 titles" rather than a bare number.
