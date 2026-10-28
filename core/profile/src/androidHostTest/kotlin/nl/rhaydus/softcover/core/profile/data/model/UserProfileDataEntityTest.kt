package nl.rhaydus.softcover.core.profile.data.model

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserProfileDataEntityTest {
    private val lenientJson = Json { ignoreUnknownKeys = true }

    @Nested
    inner class BackwardCompat {
        @Test
        fun `JSON predating username decodes it as empty`() {
            // ----- Arrange -----
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader"
                }
            """.trimIndent()

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )

            // ----- Assert -----
            entity.username shouldBe ""
        }

        @Test
        fun `a cache written by the previous build keeps its identity and ignores the dropped keys`() {
            // ----- Arrange -----
            val previousBuildJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "username": "cinque",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "readingStreak": 5,
                  "recentReadingDays": ["2026-05-04"],
                  "booksByYear": [{ "year": 2026, "count": 3 }]
                }
            """.trimIndent()

            // ----- Act -----
            val model = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                previousBuildJson,
            ).toModel()

            // ----- Assert -----
            model shouldBe UserProfileData(
                profileImageUrl = "https://example.com/avatar.png",
                name = "Jane Doe",
                username = "cinque",
                bio = "Avid reader",
            )
        }
    }
}
