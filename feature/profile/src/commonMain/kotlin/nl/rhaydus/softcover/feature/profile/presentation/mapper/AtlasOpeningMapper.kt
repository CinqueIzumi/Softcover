package nl.rhaydus.softcover.feature.profile.presentation.mapper

import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasOpeningUiModel

/** A null [profileData] yields the loading tiles. */
internal fun atlasOpeningUiModel(profileData: UserProfileData?): AtlasOpeningUiModel {
    val isLoading = profileData == null

    return AtlasOpeningUiModel(
        hero = StatTileUiModel(
            eyebrow = "Pages read",
            stat = StatNumberUiModel(
                value = (profileData?.totalPagesRead ?: 0).toDouble(),
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Hero(caption = finishedBooksCaption(profileData?.booksRead)),
        ),
        books = StatTileUiModel(
            eyebrow = "Books",
            stat = StatNumberUiModel(
                value = (profileData?.booksRead ?: 0).toDouble(),
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Standard(caption = "books finished"),
        ),
    )
}

private fun finishedBooksCaption(booksRead: Int?): String = when (booksRead) {
    null -> "From your finished books."
    1 -> "From 1 finished book."
    else -> "From ${formatGroupedNumber(booksRead)} finished books."
}
