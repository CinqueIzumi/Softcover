---
name: architecture-new-component-shadows-foundation
description: A new :core:component reimplements a foundation component's exact anatomy to bolt on one extra behavior the foundation lacks
metadata:
  type: feedback
---

When a new `:core:component` composable's render body (bar dimensions, colors, spacing, typography roles)
matches an existing `nl.rhaydus.designsystem.*` component fixture-for-fixture, grep
`docs/rhaydus/0.3.1/CAPABILITIES.md` and the app for that foundation symbol before accepting the new one —
don't just trust that the new component is additive because it adds one thing (an animation, a variant) the
foundation symbol doesn't have.

**Why:** caught `SectionHeaderUiModel.Section` (`core/component/header/SectionHeader.kt`) hand-rolling the
exact 4dp rounded primary bar / 12dp gap / uppercase eyebrow / headline / description anatomy of the
foundation's `EditorialSectionHeader` (`designsystem-editorial`, already used at ~40 call sites app-wide),
solely to layer a `pulseKey` pulse animation the foundation version doesn't support. The right shape is to
wrap/compose the foundation component and add the pulse as a decoration around it, or file an
F-numbered upstream candidate in `docs/working/foundation-upstream-candidates.md` to add pulse support
there — not carry a second full implementation of the same visual contract in the app.

**How to apply:** when a new component's KDoc or the brief mentions matching a foundation component's
"anatomy," treat that as a cue to check whether the foundation component itself could be reused (even
partially) instead of re-derived. This is the "reuse-first" `CAPABILITIES.md` rule applied to a component
that isn't a 1:1 dupe on the surface — only on close reading of the render code.
