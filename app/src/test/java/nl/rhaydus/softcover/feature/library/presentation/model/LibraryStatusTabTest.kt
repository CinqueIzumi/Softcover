package nl.rhaydus.softcover.feature.library.presentation.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryStatusTabTest {

    @Nested
    inner class Label {

        @Test
        fun `ALL has label All`() {
            // ----- Arrange -----
            val tab = LibraryStatusTab.ALL

            // ----- Act -----
            val result = tab.label

            // ----- Assert -----
            result shouldBe "All"
        }

        @Test
        fun `WANT_TO_READ has label Want to Read`() {
            // ----- Arrange -----
            val tab = LibraryStatusTab.WANT_TO_READ

            // ----- Act -----
            val result = tab.label

            // ----- Assert -----
            result shouldBe "Want to Read"
        }

        @Test
        fun `CURRENTLY_READING has label Currently Reading`() {
            // ----- Arrange -----
            val tab = LibraryStatusTab.CURRENTLY_READING

            // ----- Act -----
            val result = tab.label

            // ----- Assert -----
            result shouldBe "Currently Reading"
        }

        @Test
        fun `READ has label Read`() {
            // ----- Arrange -----
            val tab = LibraryStatusTab.READ

            // ----- Act -----
            val result = tab.label

            // ----- Assert -----
            result shouldBe "Read"
        }

        @Test
        fun `DID_NOT_FINISH has label Did Not Finish`() {
            // ----- Arrange -----
            val tab = LibraryStatusTab.DID_NOT_FINISH

            // ----- Act -----
            val result = tab.label

            // ----- Assert -----
            result shouldBe "Did Not Finish"
        }

        @Test
        fun `OWNED has label Owned`() {
            // ----- Arrange -----
            val tab = LibraryStatusTab.OWNED

            // ----- Act -----
            val result = tab.label

            // ----- Assert -----
            result shouldBe "Owned"
        }
    }

    @Nested
    inner class Entries {

        @Test
        fun `entries contains all six tabs`() {
            // ----- Arrange -----
            val expectedTabs = listOf(
                LibraryStatusTab.ALL,
                LibraryStatusTab.WANT_TO_READ,
                LibraryStatusTab.CURRENTLY_READING,
                LibraryStatusTab.READ,
                LibraryStatusTab.DID_NOT_FINISH,
                LibraryStatusTab.OWNED,
            )

            // ----- Act -----
            val result = LibraryStatusTab.entries

            // ----- Assert -----
            result shouldBe expectedTabs
        }

        @Test
        fun `WANT_TO_READ is at index 1`() {
            // ----- Arrange -----
            val tab = LibraryStatusTab.WANT_TO_READ

            // ----- Act -----
            val result = LibraryStatusTab.entries.indexOf(tab)

            // ----- Assert -----
            result shouldBe 1
        }
    }
}
