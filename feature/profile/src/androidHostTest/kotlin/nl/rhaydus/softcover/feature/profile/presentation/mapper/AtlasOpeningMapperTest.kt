package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasOpeningMapperTest {
    private fun buildProfileData(booksRead: Int = 42): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = booksRead,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 7,
    )

    @Nested
    inner class AtlasOpeningUiModelMapping {
        @Test
        fun `null profile data yields two zero-valued loading stats`() {
            // ----- Arrange -----
            val profileData: UserProfileData? = null

            // ----- Act -----
            val result = atlasOpeningUiModel(profileData)

            // ----- Assert -----
            val stats = listOf(result.hero.stat, result.books.stat)
            stats.map { it.value } shouldBe listOf(0.0, 0.0)
            stats.map { it.isLoading } shouldBe listOf(true, true)
        }

        @Test
        fun `null profile data gives the hero a caption that names no count`() {
            // ----- Arrange -----
            val profileData: UserProfileData? = null

            // ----- Act -----
            val result = atlasOpeningUiModel(profileData)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From your finished books."
        }

        @Test
        fun `profile data fills the pages and books tiles and is not loading`() {
            // ----- Arrange -----
            val profileData = buildProfileData()

            // ----- Act -----
            val result = atlasOpeningUiModel(profileData)

            // ----- Assert -----
            result.hero.stat.value shouldBe 12000.0
            result.books.stat.value shouldBe 42.0
            listOf(result.hero.stat, result.books.stat).map { it.isLoading } shouldBe listOf(false, false)
        }

        @Test
        fun `a single finished book uses the singular caption`() {
            // ----- Arrange -----
            val profileData = buildProfileData(booksRead = 1)

            // ----- Act -----
            val result = atlasOpeningUiModel(profileData)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From 1 finished book."
        }

        @Test
        fun `several finished books use the plural caption with the count`() {
            // ----- Arrange -----
            val profileData = buildProfileData(booksRead = 42)

            // ----- Act -----
            val result = atlasOpeningUiModel(profileData)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From 42 finished books."
        }

        @Test
        fun `zero finished books use the plural caption`() {
            // ----- Arrange -----
            val profileData = buildProfileData(booksRead = 0)

            // ----- Act -----
            val result = atlasOpeningUiModel(profileData)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From 0 finished books."
        }
    }
}
