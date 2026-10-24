package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileUiStateTest {
    private val readingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
    )

    private val profileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 7,
    )

    @Nested
    inner class CanShareProperty {
        @Test
        fun `is true when both the reading life and the profile have loaded`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                readingLife = readingLife,
                userProfileData = profileData,
            )

            // ----- Act -----
            val result = state.canShare

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when the reading life is missing`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                readingLife = null,
                userProfileData = profileData,
            )

            // ----- Act -----
            val result = state.canShare

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when the profile is missing`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                readingLife = readingLife,
                userProfileData = null,
            )

            // ----- Act -----
            val result = state.canShare

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class IsReadingLifeLoadingProperty {
        @Test
        fun `is true while the profile is still loading even with a reading life`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = true,
                readingLife = readingLife,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true when the reading life is missing`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = false,
                readingLife = null,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false once both have loaded`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = false,
                readingLife = readingLife,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe false
        }
    }
}
