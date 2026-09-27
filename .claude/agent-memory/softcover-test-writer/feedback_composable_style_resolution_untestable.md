---
name: feedback_composable_style_resolution_untestable
description: core:component has no compose-ui-test dependency — @Composable style-resolution functions (e.g. chipStyleFor, PageMastheadDimensions.forSize) can't be unit-tested directly; proxy via enum coverage, or flag that dropping @Composable in favor of a plain parameter is the real fix
metadata:
  type: feedback
---

`core:component`'s `androidHostTest` source set has no `compose-ui-test` / `createComposeRule` dependency
anywhere in the repo (checked by grep — zero hits). A private `@Composable` function that resolves a look
from a model field (e.g. `Chip.kt`'s `chipStyleFor(tone, selected): ChipStyle`, reading `MaterialTheme.colorScheme`)
cannot be invoked from a plain JVM unit test — it needs a composition.

**Why:** a brief asked for a `ChipUiModelTest` to cover "style resolution" after `ChipVariant` (a plain data
sealed interface) was replaced by `ChipTone` (an enum that a composable function switches on to resolve
`ChipStyle`). The old test never tested style resolution either — `ChipVariant` carried no resolution logic,
only data — so there was no precedent for reaching into the composable.

**How to apply:** when a brief or model implies testing "style resolution" for a family whose actual resolver
is a private `@Composable`, don't try to add a compose-ui-test dependency or reflectively invoke the private
function. Instead test the thing that *is* unit-testable and stands in for coverage: that every enum value
feeding the resolver (here, every `ChipTone`) appears in the model's `previews` fixture list, alongside every
sealed-slot subtype (`ChipLeading`, `ChipTrailing`). That mirrors what the old variant-coverage test did and
is a legitimate, deterministic proxy — flag the resolver itself as untested-at-unit-level rather than
fabricating a way to exercise it.

**Resolved case (`PageMastheadDimensions.forSize`):** flagged this same blocker for a `@Composable`
`forSize(size)` reading `MaterialTheme.editorialTypography`. The production fix (not mine to make): drop
`@Composable` and take the resolved typography value as a plain parameter — `forSize(size, typography)`.
That turns it into a pure function, directly testable. Its type-building factory
(`buildEditorialTypography`) was `internal` to a different Gradle module (`:core:designsystem`), invisible
from `:core:component`'s test source set — Kotlin `internal` is per-module, not per source tree — so the
test built the typography fixture directly via its public data-class constructor, giving each `TextStyle`
field a distinct value so role-mapping assertions can't pass by accident. When flagging a resolver as
untestable, name this de-composable-ify move as the structural fix, not just "add compose-ui-test."
