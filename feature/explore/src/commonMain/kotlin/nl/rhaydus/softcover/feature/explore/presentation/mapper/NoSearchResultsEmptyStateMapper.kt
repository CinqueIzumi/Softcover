package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.softcover.core.component.state.EmptyStateButtonUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant

internal fun noSearchResultsEmptyState(): EmptyStateUiModel = EmptyStateUiModel(
    title = "Nothing matched",
    body = "Try a different spelling, or a broader term.",
    variant = EmptyStateVariant.Centered(
        button = EmptyStateButtonUiModel(label = "Clear search"),
    ),
)
