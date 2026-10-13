---
name: architecture_new_shared_component_missing_catalogue_line
description: a promoted-to-:core:component row/section can land with a module-structure.md entry but no docs/reference/design-system/components.md line
metadata:
  type: architecture
---

`docs/reference/module-structure.md`'s `+`/`·` package-layout table is easy to keep current because the
step brief usually asks for it directly. `docs/reference/design-system/components.md` (and the matching
`components/<family>.md` detail file, when the family needs one) is a separate file nobody's checklist
names, so a genuinely new shared component — not a tweak to an existing one — can land fully wired
(gallery entry, fixtures, module-structure line) while the catalogue itself has zero mention of it.

**Why:** seen when a sidebar-row component was promoted out of two screen-local composables into
`core/component/sidebar/`: `module-structure.md` got its new row, the `GalleryRegistry` got its entry,
but `components.md`'s "Navigation & chrome" section — which already lists an unrelated, similarly-named
component — never got a line for the new one.

**How to apply:** when a diff adds a new top-level symbol under `core/component/<family>/` (not just a
new variant of an existing one), grep `docs/reference/design-system/components.md` for the component's
name before concluding the design-system maintenance rule (`.claude/rules/compose-ui.md`) is satisfied.
A `module-structure.md` update alone does not satisfy it — flag a missing catalogue line as 🔴 per that
rule.
