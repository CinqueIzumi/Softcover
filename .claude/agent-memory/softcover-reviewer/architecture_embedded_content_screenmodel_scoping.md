---
name: architecture_embedded_content_screenmodel_scoping
description: A plain composable embedded in another Screen's content that fetches its own ScreenModel via koinNavigatorScreenModel scopes it to the whole Navigator, not the embedding Screen
metadata:
  type: architecture
---

Canonical TOAD wiring (`docs/rhaydus/0.3.1/toad-architecture.md`): only a `Screen` calls
`koinScreenModel<XScreenModel>()` in its own `Content()`; that ties the model's lifetime to that Screen
instance being on the backstack (disposed on pop). `koinNavigatorScreenModel` (`Navigator` receiver) instead
ties the model to the Navigator's own lifetime — it survives across every Screen pushed/popped on that
Navigator, including screens the composable isn't even part of.

**Why:** a plain `@Composable` embedded in another feature's Screen content (e.g. rendered through a Koin
content seam, not itself pushed as a `Screen`) has no `Screen` receiver, so it's tempting to reach for
`LocalNavigator.currentOrThrow.koinNavigatorScreenModel<T>()` instead. That silently widens the model's scope
from "this section is visible" to "this Navigator exists at all" — inconsistent with sibling Screens in the
same family that use `koinScreenModel` and get disposed on pop. Currently harmless only when the wrapped state
is fully static (no events, nothing ever mutates); becomes a real leak/staleness risk the moment someone adds
dynamic state to that ScreenModel.

**How to apply:** flag `koinNavigatorScreenModel` (or any Navigator-scoped model fetch) coming from a
composable that is not itself the `Screen.Content()` entry point. The clean fix is for the *owning* `Screen`
to fetch the ScreenModel via `koinScreenModel` and pass the resulting model down as a parameter to the
embedded composable (R11 — render receives the model, doesn't fetch it), not for the embedded composable to
reach for its own Navigator-scoped instance.
