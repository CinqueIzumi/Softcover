---
name: UiState field never populated
description: A new UiState field can be declared and never written by any action or collector with every gate passing; grep its writers.
metadata:
  type: project
---

A `UiState` field that is declared, rendered, and never set by any action, collector or ScreenModel compiles and passes every gate — the render just shows the default forever.

**Why:** no build gate tracks writes to a state field; only review catches it.

**How to apply:** for each new `UiState` field in scope, `git grep` for a `copy(<field> =` / `setState` write; flag one with no writer.
