package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ActiveFilterChipsSnapshotTest {
    @Nested
    inner class Compute {
        @Test
        fun `empty filtersByTab produces an empty per-tab map`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(filtersByTab = emptyMap())

            // ----- Act -----
            val chipsByTab = snapshot.compute()

            // ----- Assert -----
            chipsByTab shouldBe emptyMap()
        }

        @Test
        fun `builds one model per tab keyed by tab id`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(
                filtersByTab = mapOf(
                    "all" to LibraryFilters(formats = setOf("ebook")),
                    "read" to LibraryFilters(readYear = 2021),
                ),
            )

            // ----- Act -----
            val chipsByTab = snapshot.compute()

            // ----- Assert -----
            chipsByTab.keys shouldBe setOf("all", "read")
            chipsByTab.getValue("all").chips.chips.map { it.key } shouldBe listOf("format:ebook")
            chipsByTab.getValue("read").chips.chips.map { it.key } shouldBe listOf("readYear:2021")
        }

        @Test
        fun `a tab with no active filters maps to an empty model`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(filtersByTab = mapOf("all" to LibraryFilters()))

            // ----- Act -----
            val chipsByTab = snapshot.compute()

            // ----- Assert -----
            chipsByTab.getValue("all").chips.chips shouldBe emptyList()
            chipsByTab.getValue("all").clearAll shouldBe null
        }

        @Test
        fun `each tab's ChipSet resolves its own chip keys back to the value that built them`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(
                filtersByTab = mapOf(
                    "all" to LibraryFilters(formats = setOf("ebook")),
                    "read" to LibraryFilters(readYear = 2021),
                ),
            )

            // ----- Act -----
            val chipsByTab = snapshot.compute()

            // ----- Assert -----
            chipsByTab.getValue("all").chips["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
            chipsByTab.getValue("read").chips["readYear:2021"] shouldBe LibraryFilterValue.ReadYear(year = 2021)
        }

        @Test
        fun `two tabs sharing the same active value each keep their own independent ChipSet entry`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(
                filtersByTab = mapOf(
                    "all" to LibraryFilters(formats = setOf("ebook")),
                    "read" to LibraryFilters(formats = setOf("ebook")),
                ),
            )

            // ----- Act -----
            val chipsByTab = snapshot.compute()

            // ----- Assert -----
            chipsByTab.getValue("all").chips["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
            chipsByTab.getValue("read").chips["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
        }
    }
}
