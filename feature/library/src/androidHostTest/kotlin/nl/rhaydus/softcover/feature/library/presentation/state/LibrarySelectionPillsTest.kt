package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibrarySelectionPillsTest {
    @Nested
    inner class Of {
        @Test
        fun `every pill is disabled while a bulk action is in progress`() {
            // ----- Arrange -----
            val bulkActionInProgress = true

            // ----- Act -----
            val pills = LibrarySelectionPills.of(bulkActionInProgress = bulkActionInProgress)

            // ----- Assert -----
            listOf(pills.exit, pills.move, pills.addToList, pills.remove).map { it.enabled } shouldBe List(4) { false }
        }

        @Test
        fun `every pill is clickable when no bulk action is in progress`() {
            // ----- Arrange -----
            val bulkActionInProgress = false

            // ----- Act -----
            val pills = LibrarySelectionPills.of(bulkActionInProgress = bulkActionInProgress)

            // ----- Assert -----
            listOf(pills.exit, pills.move, pills.addToList, pills.remove).map { it.enabled } shouldBe List(4) { true }
        }

        @Test
        fun `the four pills have distinct keys`() {
            // ----- Arrange -----
            val pills = LibrarySelectionPills.of(bulkActionInProgress = false)

            // ----- Act -----
            val keys = listOf(pills.exit, pills.move, pills.addToList, pills.remove).map { it.key }

            // ----- Assert -----
            keys.toSet().size shouldBe 4
        }
    }
}
