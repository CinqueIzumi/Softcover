---
name: feedback_composable_style_resolution_untestable
description: core:component has no compose-ui-test dependency — private @Composable style-resolution functions (e.g. chipStyleFor) can't be unit-tested directly; test the enum coverage that drives them instead
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
