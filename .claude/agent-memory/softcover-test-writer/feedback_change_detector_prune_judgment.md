---
name: feedback_change_detector_prune_judgment
description: How to judge mixed-content test files during a change-detector prune pass (`.claude/rules/tests.md`) — filename patterns are examples, not literal matches
metadata:
  type: feedback
---

When a prune brief lists filename patterns (`*Colors*Test`, `*Dimensions*Test`, `*Style*Test`) as deletion
examples, treat them as illustrative, not a literal filename match — judge each `@Nested` block or test by
the actual rule (never assert a value obtainable only by copying a literal out of production code), not by
the file's name.

- A `tone/backdrop -> ColorScheme role` or `size -> dp/typography` lookup (`BadgeColors.of`,
  `SelectionMarkColors.of`, `DividerInk.resolve`, `sectionHeaderToneColor`) is a style table even though it
  has a real `when` branch — delete it. The branch selects between literals, it doesn't compute anything.
- A `width -> dp/sp/Int` function with floor/ceiling clamps and proportional interpolation
  (`monogramCoverMetrics`) or a `progress -> Float` easing/envelope function with real clamping
  (`sectionHeaderPulseEnvelope`) is genuine algorithmic logic even though it lives in a file named
  `*StyleTest` or `*MetricsTest` — keep it. The boundary/interpolation values it asserts come from the
  algorithm's behavior, not from copying a style-table literal.
- A two-field `UiModel` whose `previews` list has no enum/sealed variant to enumerate (e.g.
  `MastheadRefreshUiModel`, just `label` + `enabled`) can only be fixture-covered by asserting the literal
  fixture content (`previews[0].label shouldBe "Refresh"`) — keep that, it matches the same "every fixture
  covered" precedent as `BadgeUiModel`'s `withIcon.size shouldBe 2`. Constructed-value equality tests
  (`data class` `equals`/`hashCode` on ad-hoc instances, not sourced from `previews`) are low-value but don't
  match either deletion criterion (no copied literal, no constructor default) — leave them, note as
  borderline.
- A `defaults to X` test that chains one default param into another (e.g. a field whose default value is
  built from two other fields that are *also* left at their defaults) is still a constructor-default test —
  delete it — unless the test supplies an explicit non-default input to prove the derivation, not just calls
  the zero-arg constructor.

**Why:** a filename-pattern-only pass both over-deletes (real algorithmic logic in a `*StyleTest`/
`*MetricsTest` file) and under-deletes (a style-table lookup that happens to have a `when` branch, or a
default-chained value that looks derived but isn't) — several files mix keep-worthy and delete-worthy tests
under one class name.

**How to apply:** on any change-detector prune brief, read every `@Nested` block's assertions before deciding
a file's fate; only delete a whole file when every remaining block is style-table or constructor-default
content.
