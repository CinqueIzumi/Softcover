package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
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


    @Nested
    inner class MoveTargets {
        @Test
        fun `offers every shelf when no shelf is being viewed`() {
            // ----- Arrange -----
            val currentShelf: UserBookStatus? = null

            // ----- Act -----
            val pills = LibrarySelectionPills.of(
                bulkActionInProgress = false,
                currentShelf = currentShelf,
            )

            // ----- Assert -----
            pills.moveTargets.map { it.status } shouldBe listOf(
                UserBookStatus.WANT_TO_READ,
                UserBookStatus.CURRENTLY_READING,
                UserBookStatus.PAUSED,
                UserBookStatus.READ,
            )
        }

        @Test
        fun `leaves out the shelf being viewed`() {
            // ----- Arrange -----
            val currentShelf = UserBookStatus.CURRENTLY_READING

            // ----- Act -----
            val pills = LibrarySelectionPills.of(
                bulkActionInProgress = false,
                currentShelf = currentShelf,
            )

            // ----- Assert -----
            pills.moveTargets.map { it.status } shouldBe listOf(
                UserBookStatus.WANT_TO_READ,
                UserBookStatus.PAUSED,
                UserBookStatus.READ,
            )
        }

        @Test
        fun `every shelf is dropped in turn and the rest stay`() {
            // ----- Arrange -----
            val shelves = listOf(
                UserBookStatus.WANT_TO_READ,
                UserBookStatus.CURRENTLY_READING,
                UserBookStatus.PAUSED,
                UserBookStatus.READ,
            )

            // ----- Act -----
            val offered = shelves.map { shelf ->
                val pills = LibrarySelectionPills.of(
                    bulkActionInProgress = false,
                    currentShelf = shelf,
                )
                pills.moveTargets.map { it.status }
            }

            // ----- Assert -----
            offered.forEachIndexed { index, statuses ->
                statuses shouldBe shelves - shelves[index]
            }
        }

        @Test
        fun `the move targets do not depend on the bulk action flag`() {
            // ----- Arrange -----
            val idle = LibrarySelectionPills.of(
                bulkActionInProgress = false,
                currentShelf = UserBookStatus.READ,
            )

            // ----- Act -----
            val busy = LibrarySelectionPills.of(
                bulkActionInProgress = true,
                currentShelf = UserBookStatus.READ,
            )

            // ----- Assert -----
            busy.moveTargets shouldBe idle.moveTargets
        }
    }
    }
}
