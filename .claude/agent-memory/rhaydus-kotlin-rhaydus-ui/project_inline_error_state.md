---
name: project_inline_error_state
description: InlineErrorState shared component — location, anatomy, and TOAD wiring convention
metadata:
  type: project
---

`InlineErrorState(message, onRetry, modifier)` lives at `core/designsystem/src/commonMain/kotlin/nl/rhaydus/softcover/core/designsystem/presentation/component/InlineErrorState.kt`.

Anatomy: centered Column — `MaterialTheme.editorialTypography.bodySmall` text in `error` color role, 12dp spacer, `RhaydusButton(OUTLINED, S, fillMaxWidth)` labelled "Retry".

**Why:** canonical surface for any TOAD `String?` error-slot; avoids per-screen hand-rolled error layouts.

**How to apply:** when a screen's `UiState` has a `String?` error field (e.g. `searchError`, `submissionError`), render `InlineErrorState` in place of the normal content when it is non-null. The `onRetry` lambda re-dispatches the appropriate `UiAction`. Modifier should be `Modifier.fillMaxSize()` for a full-panel takeover or left at default for inline use.

Consumed by: `ExploreScreenLayout.mobile.kt` (`ActiveSearchContent`, `searchError`/`OnRetrySearchAction`) and `OnboardingShelf.kt` (`ApiKeyEntrySection`, `submissionError`/`OnApiKeySaveClickAction`).
