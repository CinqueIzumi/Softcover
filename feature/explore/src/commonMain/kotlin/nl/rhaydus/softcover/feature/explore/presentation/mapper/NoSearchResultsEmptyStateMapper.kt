package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant

internal fun noSearchResultsEmptyState(): EmptyStateUiModel = EmptyStateUiModel(
    title = "Nothing matched",
    body = "Try a different spelling, or a broader term.",
    variant = EmptyStateVariant.Centered(
        button = PillButtonUiModel(
            key = "clear-search",
            content = PillButtonContent.Label(
                text = "Clear search",
            ),
            tone = PillButtonTone.Filled,
            size = PillButtonSize.Large,
        ),
    ),
)
