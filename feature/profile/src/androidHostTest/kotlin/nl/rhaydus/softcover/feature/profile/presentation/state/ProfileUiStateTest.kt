package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileUiStateTest {
    private val readingStats = ReadingStats()

    private val pagesRead = PagesRead(
        total = 0,
        finishedBookCount = 0,
        stoppedBookCount = 0,
        hasInProgressPages = false,
        pagesPerDay = null,
    )

    private val readingHistory = ReadingHistory()

    private val profileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
    )

    @Nested
    inner class CanShareProperty {
        @Test
        fun `is true when the reading stats, history and profile have all loaded`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                readingStats = readingStats,
                pagesRead = pagesRead,
                readingHistory = readingHistory,
                userProfileData = profileData,
            )

            // ----- Act -----
            val result = state.canShare

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when the reading stats are missing`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                readingStats = null,
                readingHistory = readingHistory,
                userProfileData = profileData,
            )

            // ----- Act -----
            val result = state.canShare

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when the reading history is missing`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                readingStats = readingStats,
                pagesRead = pagesRead,
                readingHistory = null,
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
                readingStats = readingStats,
                pagesRead = pagesRead,
                readingHistory = readingHistory,
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
        fun `is true while the profile is still loading even with stats and history`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = true,
                readingStats = readingStats,
                pagesRead = pagesRead,
                readingHistory = readingHistory,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true when the reading stats are missing`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = false,
                readingStats = null,
                readingHistory = readingHistory,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true when the reading history is missing`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = false,
                readingStats = readingStats,
                pagesRead = pagesRead,
                readingHistory = null,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true while the pages read have not loaded`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = false,
                readingStats = readingStats,
                pagesRead = null,
                readingHistory = readingHistory,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false once all of them have loaded`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                isLoading = false,
                readingStats = readingStats,
                pagesRead = pagesRead,
                readingHistory = readingHistory,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe false
        }
    }
}
