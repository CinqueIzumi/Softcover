package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingAtlasMapperTest {
    private fun buildProfileData(): UserProfileData = UserProfileData(
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
    inner class ReadingAtlasUiModelMapping {
        @Test
        fun `null profile data yields four zero-valued loading stats`() {
            // ----- Arrange -----
            val profileData: UserProfileData? = null

            // ----- Act -----
            val result = readingAtlasUiModel(profileData)

            // ----- Assert -----
            val stats = listOf(
                result.hero.stat,
                result.volumes.stat,
                result.rating.stat,
                result.streak.stat,
            )
            stats.map { it.value } shouldBe listOf(0.0, 0.0, 0.0, 0.0)
            stats.map { it.isLoading } shouldBe listOf(true, true, true, true)
        }

        @Test
        fun `profile data maps each statistic with its own format and is not loading`() {
            // ----- Arrange -----
            val profileData = buildProfileData()

            // ----- Act -----
            val result = readingAtlasUiModel(profileData)

            // ----- Assert -----
            result.hero.stat shouldBe StatNumberUiModel(
                value = 12000.0,
                format = StatNumberFormat.Grouped,
            )
            result.volumes.stat shouldBe StatNumberUiModel(
                value = 42.0,
                format = StatNumberFormat.Grouped,
            )
            result.rating.stat shouldBe StatNumberUiModel(
                value = 4.2,
                format = StatNumberFormat.Decimal,
            )
            result.streak.stat shouldBe StatNumberUiModel(
                value = 7.0,
                format = StatNumberFormat.Plain,
            )
        }
    }
}
