package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toReadingLifeShareCardUiModel

internal fun buildShareCard(name: String = "Jane Doe"): ReadingLifeShareCardUiModel =
    ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
        trackedYears = 3,
    ).toReadingLifeShareCardUiModel(
        UserProfileData(
            profileImageUrl = "https://example.com/avatar.png",
            name = name,
            username = "cinque",
            bio = "Avid reader",
            booksRead = 42,
            totalPagesRead = 12000,
            averageRating = 4.2,
            readingStreak = 7,
        ),
    )
