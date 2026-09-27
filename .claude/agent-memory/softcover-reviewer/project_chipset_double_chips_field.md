---
name: project-chipset-double-chips-field
description: ChipSet<P> names its list field `chips`, so a wrapper that also calls its own field `chips` (LibraryActiveFilterChips.chips, LibraryFilterChips.ownershipChips) forces `wrapper.chips.chips` / `wrapper.facetChips.chips` at every render call site.
metadata:
  type: project
---

`core/component/chip/ChipSet.kt` exposes its backing list as `ChipSet.chips: ImmutableList<ChipUiModel>`. Every
feature wrapper that holds a `ChipSet<P>` in a field itself named `chips` (or a facet field like
`ownershipChips`) produces `state.chips.chips.forEach { … }` / `chips.ownershipChips.chips.isNotEmpty()` at
render call sites (`LibraryFilterChipRow.kt`, `LibraryFilterSheet.kt`). Not a bug — `ChipSet` has no
`isEmpty`/`size`/`Iterable` convenience, so callers must reach through `.chips` explicitly — but it reads like
a typo and is worth a 🟡/🔵 readability note each time it recurs, plus flagging that `ChipSet` gaining
`Iterable<ChipUiModel>` and an `isEmpty`/`size` delegate (mirroring what `LibraryFilterChips.isEmpty` already
hand-rolls per facet) would remove the friction. Expect more of this as more chip/wrapper families adopt
`ChipSet`.
