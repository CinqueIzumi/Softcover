package nl.rhaydus.softcover.core.profile.data.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

class UserProfileDataEntityTest {
    private fun minimalModel(dates: Set<LocalDate> = emptySet()): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 5,
        recentReadingDays = dates,
    )

    @Nested
    inner class RoundTrip {
        @Test
        fun `toEntity then toModel preserves an empty active dates set`() {
            // ----- Arrange -----
            val model = minimalModel(dates = emptySet())

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.recentReadingDays shouldBe emptySet()
        }

        @Test
        fun `toEntity then toModel preserves a non-empty active dates set`() {
            // ----- Arrange -----
            val dates = setOf(
                LocalDate(
                    2026,
                    4,
                    14,
                ),
                LocalDate(
                    2026,
                    4,
                    20,
                ),
                LocalDate(
                    2026,
                    5,
                    4,
                ),
            )
            val model = minimalModel(dates = dates)

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.recentReadingDays shouldBe dates
        }

        @Test
        fun `toEntity then toModel preserves username`() {
            // ----- Arrange -----
            val model = minimalModel()

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.username shouldBe "cinque"
        }

        @Test
        fun `toEntity serialises dates as ascending ISO strings`() {
            // ----- Arrange -----
            val dates = setOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    4,
                    14,
                ),
                LocalDate(
                    2026,
                    4,
                    30,
                ),
            )
            val model = minimalModel(dates = dates)

            // ----- Act -----
            val entity = model.toEntity()

            // ----- Assert -----
            entity.recentReadingDays shouldBe listOf("2026-04-14", "2026-04-30", "2026-05-04")
        }
    }

    @Nested
    inner class BackwardCompat {
        @Test
        fun `legacy JSON without recentReadingDays field decodes with empty list`() {
            // ----- Arrange -----
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "totalPagesRead": 12000,
                  "averageRating": 4.2,
                  "readingStreak": 5
                }
            """.trimIndent()
            val lenientJson = Json { ignoreUnknownKeys = true }

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )

            // ----- Assert -----
            entity.recentReadingDays shouldBe emptyList()
        }

        @Test
        fun `legacy entity without recentReadingDays maps to empty set on toModel`() {
            // ----- Arrange -----
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "totalPagesRead": 12000,
                  "averageRating": 4.2,
                  "readingStreak": 5
                }
            """.trimIndent()
            val lenientJson = Json { ignoreUnknownKeys = true }

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )
            val model = entity.toModel()

            // ----- Assert -----
            model.recentReadingDays shouldBe emptySet()
        }

        @Test
        fun `malformed date strings in recentReadingDays are silently dropped on toModel`() {
            // ----- Arrange -----
            val entity = UserProfileDataEntity(
                profileImageUrl = "https://example.com/avatar.png",
                name = "Jane Doe",
                username = "cinque",
                bio = "Avid reader",
                booksRead = 42,
                totalPagesRead = 12000,
                averageRating = 4.2,
                readingStreak = 5,
                recentReadingDays = listOf("2026-05-04", "not-a-date", "2026-04-14"),
            )

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model.recentReadingDays shouldBe setOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    4,
                    14,
                ),
            )
        }
    }
}
