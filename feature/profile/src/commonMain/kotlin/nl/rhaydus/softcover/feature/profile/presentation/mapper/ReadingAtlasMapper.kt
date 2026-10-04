package nl.rhaydus.softcover.feature.profile.presentation.mapper

import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingAtlasUiModel

/** A null [profileData] yields the loading tiles. */
internal fun readingAtlasUiModel(profileData: UserProfileData?): ReadingAtlasUiModel {
    val isLoading = profileData == null

    return ReadingAtlasUiModel(
        hero = StatTileUiModel(
            eyebrow = "Total pages read",
            stat = StatNumberUiModel(
                value = (profileData?.totalPagesRead ?: 0).toDouble(),
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Hero(caption = "Every page a step further into the story."),
        ),
        volumes = StatTileUiModel(
            eyebrow = "Volumes",
            stat = StatNumberUiModel(
                value = (profileData?.booksRead ?: 0).toDouble(),
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Standard(caption = "books read"),
        ),
        rating = StatTileUiModel(
            eyebrow = "Avg. rating",
            stat = StatNumberUiModel(
                value = profileData?.averageRating ?: 0.0,
                format = StatNumberFormat.Decimal,
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Compact(unit = "★"),
        ),
        streak = StatTileUiModel(
            eyebrow = "Streak",
            stat = StatNumberUiModel(
                value = (profileData?.readingStreak ?: 0).toDouble(),
                format = StatNumberFormat.Plain,
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Compact(unit = "days"),
        ),
    )
}
