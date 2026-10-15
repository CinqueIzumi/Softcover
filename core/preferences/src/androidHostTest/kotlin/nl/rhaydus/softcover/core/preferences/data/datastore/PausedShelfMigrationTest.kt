package nl.rhaydus.softcover.core.preferences.data.datastore

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PausedShelfMigrationTest {
    @Nested
    inner class ShouldMigrate {
        @Test
        fun `migrates settings that have not been seeded`() = runTest {
            // ----- Arrange -----
            val settings = AppSettingsEntity(pausedShelfSeeded = false)

            // ----- Act -----
            val result = PausedShelfMigration.shouldMigrate(settings)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `skips settings that were already seeded`() = runTest {
            // ----- Arrange -----
            val settings = AppSettingsEntity(pausedShelfSeeded = true)

            // ----- Act -----
            val result = PausedShelfMigration.shouldMigrate(settings)

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class Migrate {
        @Test
        fun `adds the paused status code and marks the shelf seeded`() = runTest {
            // ----- Arrange -----
            val settings = AppSettingsEntity(enabledStatusCodes = setOf(1, 3, 5))

            // ----- Act -----
            val result = PausedShelfMigration.migrate(settings)

            // ----- Assert -----
            result.enabledStatusCodes shouldBe setOf(1, 3, 4, 5)
            result.pausedShelfSeeded shouldBe true
        }

        @Test
        fun `keeps a paused status code that is already enabled without duplicating it`() = runTest {
            // ----- Arrange -----
            val settings = AppSettingsEntity(enabledStatusCodes = setOf(4))

            // ----- Act -----
            val result = PausedShelfMigration.migrate(settings)

            // ----- Assert -----
            result.enabledStatusCodes shouldBe setOf(4)
        }

        @Test
        fun `inserts the paused tab right after want to read in a customised order`() = runTest {
            // ----- Arrange -----
            val settings = AppSettingsEntity(
                libraryTabOrder = listOf(
                    "status-3",
                    "status-2",
                    "status-5",
                ),
            )

            // ----- Act -----
            val result = PausedShelfMigration.migrate(settings)

            // ----- Assert -----
            result.libraryTabOrder shouldBe listOf(
                "status-3",
                "status-2",
                "status-4",
                "status-5",
            )
        }

        @Test
        fun `appends the paused tab when want to read is not in the order`() = runTest {
            // ----- Arrange -----
            val settings = AppSettingsEntity(
                libraryTabOrder = listOf(
                    "status-3",
                    "status-5",
                ),
            )

            // ----- Act -----
            val result = PausedShelfMigration.migrate(settings)

            // ----- Assert -----
            result.libraryTabOrder shouldBe listOf(
                "status-3",
                "status-5",
                "status-4",
            )
        }

        @Test
        fun `does not duplicate a paused tab that is already in the order`() = runTest {
            // ----- Arrange -----
            val order = listOf(
                "status-4",
                "status-2",
                "status-3",
            )
            val settings = AppSettingsEntity(libraryTabOrder = order)

            // ----- Act -----
            val result = PausedShelfMigration.migrate(settings)

            // ----- Assert -----
            result.libraryTabOrder shouldBe order
        }

        @Test
        fun `leaves an empty tab order untouched`() = runTest {
            // ----- Arrange -----
            val settings = AppSettingsEntity(libraryTabOrder = emptyList())

            // ----- Act -----
            val result = PausedShelfMigration.migrate(settings)

            // ----- Assert -----
            result.libraryTabOrder shouldBe emptyList()
        }
    }
}
