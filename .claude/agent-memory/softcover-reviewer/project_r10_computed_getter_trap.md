---
name: project_r10_computed_getter_trap
description: A UiState `get()` property that assembles a UI model (e.g. a header) still violates R10, even though it reads like "a UiState field"
metadata:
  type: project
---

R10 (`docs/reference/design-system/component-contract.md`) requires a UI model to be a **stored** field set
by a collector or a constructor default, never built at the point the composable reads it. A `val foo: FooUiModel
get() = FooUiModel(...)` on a `UiState` satisfies the letter ("it's a UiState field") but not the rule: the
constructor runs every time the property is accessed, i.e. on every render read — functionally identical to
building it inline in the composable, just relocated one hop.

**Why:** seen on a header-migration pass — a computed property wrapping a static header plus mutable
`progress`/`isLoading` fields, while sibling `UiState`s in the same family used a real stored constructor field
with a default value instead.

**How to apply:** when a header/model is a pure constant, it should be a stored default-value field
(`val header: FooUiModel.Section = FooUiModel.Section(...)`). When it's a composite of a constant part and
fields that change over time (progress, a count, …), the fix is to store the *whole* composite model as one
field and have the action/collector `copy()` into it
(`it.copy(loadingSheet = it.loadingSheet.copy(progress = 0.4f))`) rather than keeping separate primitives that
get recombined in a getter. Flag any `UiState` with a `get()`-bodied property that constructs a `*UiModel`.
