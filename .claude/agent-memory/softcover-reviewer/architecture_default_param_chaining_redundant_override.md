---
name: default-param-chaining-redundant-override
description: a UiState default that interpolates an earlier constructor param already auto-updates when that param is supplied; a ScreenModel that also passes the derived field explicitly is redundant, not defensive
metadata:
  type: architecture
---

Kotlin default-parameter expressions may reference *preceding* parameters in the same constructor, and at
a call site they resolve against whatever value that preceding parameter actually received — default or
explicit — not against that parameter's own default. So `val b: T = fn(a)` as a default, with `a` declared
earlier, already recomputes correctly whenever a caller supplies `a` and omits `b`.

Trap: a ScreenModel's `initialState = FooUiState(a = real, b = deriveFromReal(a))` looks defensive but is
dead-weight duplication when `b`'s own default in the data class already does `deriveFromReal(a)` — the
same format/derivation string now lives in two places that must be kept hand-in-sync, and a test built by
calling the constructor with both `a` and an explicit `b` can't tell the difference (it would pass either
way). Check parameter order (the deriving field must be declared after the field(s) it reads) before
concluding a "fabricated" literal default (e.g. `"Version  (0)"` for empty `""`/`0`) is actually the risk —
the real risk is usually the *duplicated derivation*, not the placeholder text itself, which typically
only ever surfaces via a bare no-arg constructor call in a test or forgotten preview.

See [[project_r10_computed_getter_trap]] for the sibling case (a `get()` that builds a UI model, forbidden
outright); this is the opposite shape — a stored default that's legal but silently redundant with an
explicit override elsewhere.
