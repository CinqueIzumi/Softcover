package nl.rhaydus.softcover.core.profile.data.model

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
        activeReadingDates = dates,
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
            result.activeReadingDates shouldBe emptySet()
        }

        @Test
        fun `toEntity then toModel preserves a non-empty active dates set`() {
            // ----- Arrange -----
            val dates = setOf(
                LocalDate.of(2026, 4, 14),
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 5, 4),
            )
            val model = minimalModel(dates = dates)

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.activeReadingDates shouldBe dates
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
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 4, 14),
                LocalDate.of(2026, 4, 30),
            )
            val model = minimalModel(dates = dates)

            // ----- Act -----
            val entity = model.toEntity()

            // ----- Assert -----
            entity.activeReadingDates shouldBe listOf("2026-04-14", "2026-04-30", "2026-05-04")
        }
    }

    @Nested
    inner class BackwardCompat {

        @Test
        fun `legacy JSON without activeReadingDates field decodes with empty list`() {
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
            val entity = lenientJson.decodeFromString(UserProfileDataEntity.serializer(), legacyJson)

            // ----- Assert -----
            entity.activeReadingDates shouldBe emptyList()
        }

        @Test
        fun `legacy entity without activeReadingDates maps to empty set on toModel`() {
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
            val entity = lenientJson.decodeFromString(UserProfileDataEntity.serializer(), legacyJson)
            val model = entity.toModel()

            // ----- Assert -----
            model.activeReadingDates shouldBe emptySet()
        }

        @Test
        fun `malformed date strings in activeReadingDates are silently dropped on toModel`() {
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
                activeReadingDates = listOf("2026-05-04", "not-a-date", "2026-04-14"),
            )

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model.activeReadingDates shouldBe setOf(
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 4, 14),
            )
        }
    }
}
