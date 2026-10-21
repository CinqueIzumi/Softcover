package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.SystemVibe

internal fun SystemVibe.toRecommendedHeader(isLoading: Boolean): SectionHeaderUiModel = when (this) {
    SystemVibe.Recommendations -> SectionHeaderUiModel.Section(
        eyebrow = "Recommended for you",
        headline = "Based on what you've read",
        isLoading = isLoading,
    )

    SystemVibe.TopPicks -> SectionHeaderUiModel.Section(
        eyebrow = "Top picks",
        headline = "Popular with readers right now",
        isLoading = isLoading,
    )
}
