---
name: project_private_data_class_colocation
description: A private helper data class colocated in a shared component file (not its own file) is an established, tool-accepted pattern in this codebase — do not flag it as a one-type-per-file violation.
metadata:
  type: project
---

The foundation code-style guide's "one data class per file" rule reads as absolute ("a single-caller
data class still gets its own file next to that caller"), but in practice this codebase already has
multiple `private data class` helpers colocated inside a `core/designsystem` component file whose
primary top-level declaration is a `@Composable fun`, not a class:

- `EditionImage.kt` → `private data class EditionImageResolution`
- `MarkAsReadBurst.kt` → `private data class ParticleSeed`
- `ChooseListsBottomSheet.kt` → `private data class ListMembershipInfo` (added in the 2026-07-22
  Choose-lists/Change-edition bottom-sheet redesign, reviewed clean)

All of these passed `ktlintCheck`'s "one-type-per-file" gate, so the rule apparently does not fire on
a private, single-file-scoped data class that exists purely as a helper-function return type inside a
component file (as opposed to two unrelated public/exported classes sharing a file). Treat this as the
accepted pattern — do not flag a new instance of it as a style violation on the strength of the guide's
prose alone; verify against `ktlintCheck` behavior / this precedent first.

See also [[style_conventions]].
