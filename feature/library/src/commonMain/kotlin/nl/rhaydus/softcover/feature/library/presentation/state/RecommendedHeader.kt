package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.SystemVibe

internal fun SystemVibe.toRecommendedHeader(isLoading: Boolean): SectionHeaderUiModel = when (this) {
    SystemVibe.Recommendations -> SectionHeaderUiModel.Section(
        eyebrow = "Not on your shelf yet",
        headline = "Recommended for you",
        isLoading = isLoading,
    )

    SystemVibe.TopPicks -> SectionHeaderUiModel.Section(
        eyebrow = "Not on your shelf yet",
        headline = "Top picks",
        isLoading = isLoading,
    )
}
