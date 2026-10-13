---
name: project_component_migration_style_drops
description: Migrating a screen's bespoke header/text onto a shared core/component UI model can silently drop more than one styling property — check all of them, not just the one flagged
metadata:
  type: project
---

Migrating settings' "Your tabs" header onto `SectionHeaderUiModel.Label` dropped both its
`letterSpacing = 2.2.sp` *and* its `color = MaterialTheme.colorScheme.primary` (the shared
`SectionHeaderLabel` renders `onSurfaceVariant`). The brief that reached the implementer only
named the letter-spacing loss; the color loss was found by diffing against `git show HEAD:<old
path>` but was left for a follow-up since fixing it meant adding a color override to a shared UI
model used by other call sites — out of scope for a two-line brief.

**Why:** component-library migration work moves many bespoke `Text`/`Row` blocks onto shared UI
models one call site at a time; each step's brief tends to name only the one regression a
reviewer already spotted, not a full audit.

**How to apply:** when a brief says "check the old version to confirm nothing else was lost," do
a full property-by-property diff (color, weight, size, spacing — not just the one named), and
report every other loss found under **Left over** even if fixing it is out of the brief's scope.
Related: [[feedback_check_working_tree_before_starting]].
