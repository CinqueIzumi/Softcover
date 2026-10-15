package nl.rhaydus.softcover.core.preferences.data.datastore.serializer

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import nl.rhaydus.softcover.core.preferences.data.model.ThemeConfigurationEntity
import okio.Buffer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppSettingsSerializerTest {
    private suspend fun read(json: String): AppSettingsEntity {
        val source = Buffer().writeUtf8(json)

        return AppSettingsSerializer.readFrom(source)
    }

    @Nested
    inner class ReadFrom {
        @Test
        fun `unknown theme mode falls back to default while other fields survive`() = runTest {
            // ----- Arrange -----
            val json = """
                {
                  "userId": 42,
                  "themeConfig": {"themeMode": "SEPIA", "colorPalette": "SEA"},
                  "librarySortModeByTab": {"reading": "TITLE"},
                  "libraryTabOrder": ["b", "a"]
                }
            """.trimIndent()

            // ----- Act -----
            val result = read(json)

            // ----- Assert -----
            result shouldBe AppSettingsEntity(
                userId = 42,
                themeConfig = ThemeConfigurationEntity(
                    themeMode = ThemeMode.DEFAULT,
                    colorPalette = ColorPalette.SEA,
                ),
                librarySortModeByTab = mapOf("reading" to LibrarySortMode.TITLE),
                libraryTabOrder = listOf("b", "a"),
            )
        }

        @Test
        fun `unknown keys are ignored`() = runTest {
            // ----- Arrange -----
            val json = """{"userId": 7, "somethingFromTheFuture": {"a": 1}}"""

            // ----- Act -----
            val result = read(json)

            // ----- Assert -----
            result shouldBe AppSettingsEntity(userId = 7)
        }

        @Test
        fun `sort mode map drops only the unknown value`() = runTest {
            // ----- Arrange -----
            val json = """
                {"librarySortModeByTab": {"a": "TITLE", "b": "NOT_A_MODE", "c": "RATING"}}
            """.trimIndent()

            // ----- Act -----
            val result = read(json)

            // ----- Assert -----
            result.librarySortModeByTab shouldBe mapOf(
                "a" to LibrarySortMode.TITLE,
                "c" to LibrarySortMode.RATING,
            )
        }

        @Test
        fun `sort direction map drops only the unknown value`() = runTest {
            // ----- Arrange -----
            val json = """
                {"librarySortDirectionByTab": {"a": "ASCENDING", "b": "SIDEWAYS", "c": "DESCENDING"}}
            """.trimIndent()

            // ----- Act -----
            val result = read(json)

            // ----- Assert -----
            result.librarySortDirectionByTab shouldBe mapOf(
                "a" to SortDirection.ASCENDING,
                "c" to SortDirection.DESCENDING,
            )
        }

        @Test
        fun `malformed json falls back to the default entity`() = runTest {
            // ----- Arrange -----
            val json = "{ this is not json"

            // ----- Act -----
            val result = read(json)

            // ----- Assert -----
            result shouldBe AppSettingsEntity()
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `written settings decode back to an equal entity`() = runTest {
            // ----- Arrange -----
            val entity = AppSettingsEntity(
                userId = 9,
                themeConfig = ThemeConfigurationEntity(
                    themeMode = ThemeMode.DARK,
                    colorPalette = ColorPalette.INK,
                    useDynamicColor = true,
                ),
                librarySortModeByTab = mapOf(
                    "reading" to LibrarySortMode.AUTHOR,
                    "read" to LibrarySortMode.DATE_FINISHED,
                ),
                librarySortDirectionByTab = mapOf(
                    "reading" to SortDirection.DESCENDING,
                    "read" to SortDirection.ASCENDING,
                ),
                dismissedPlanTodayByBook = mapOf(1 to "2026-01-01"),
                libraryTabOrder = listOf("read", "reading"),
                becauseYouReadGenre = "Fantasy",
            )
            val buffer = Buffer()

            // ----- Act -----
            AppSettingsSerializer.writeTo(
                t = entity,
                sink = buffer,
            )
            val result = AppSettingsSerializer.readFrom(buffer)

            // ----- Assert -----
            result shouldBe entity
        }
    }
}
