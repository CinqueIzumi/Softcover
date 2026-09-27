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
        fun `empty filtersByTab produces an empty per-tab map and an empty value map`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(filtersByTab = emptyMap())

            // ----- Act -----
            val (chipsByTab, valueByKey) = snapshot.compute()

            // ----- Assert -----
            chipsByTab shouldBe emptyMap()
            valueByKey shouldBe emptyMap()
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
            val (chipsByTab, _) = snapshot.compute()

            // ----- Assert -----
            chipsByTab.keys shouldBe setOf("all", "read")
            chipsByTab.getValue("all").chips.map { it.key } shouldBe listOf("format:ebook")
            chipsByTab.getValue("read").chips.map { it.key } shouldBe listOf("readYear:2021")
        }

        @Test
        fun `a tab with no active filters maps to an empty model`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(filtersByTab = mapOf("all" to LibraryFilters()))

            // ----- Act -----
            val (chipsByTab, _) = snapshot.compute()

            // ----- Assert -----
            chipsByTab.getValue("all").chips shouldBe emptyList()
            chipsByTab.getValue("all").clearAll shouldBe null
        }

        @Test
        fun `flattens every tab's value map into one lookup across tabs`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(
                filtersByTab = mapOf(
                    "all" to LibraryFilters(formats = setOf("ebook")),
                    "read" to LibraryFilters(readYear = 2021),
                ),
            )

            // ----- Act -----
            val (_, valueByKey) = snapshot.compute()

            // ----- Assert -----
            valueByKey["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
            valueByKey["readYear:2021"] shouldBe LibraryFilterValue.ReadYear(year = 2021)
        }

        @Test
        fun `two tabs sharing the same active value collapse to one entry in the flattened map`() {
            // ----- Arrange -----
            val snapshot = ActiveFilterChipsSnapshot(
                filtersByTab = mapOf(
                    "all" to LibraryFilters(formats = setOf("ebook")),
                    "read" to LibraryFilters(formats = setOf("ebook")),
                ),
            )

            // ----- Act -----
            val (_, valueByKey) = snapshot.compute()

            // ----- Assert -----
            valueByKey.size shouldBe 1
            valueByKey["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
        }
    }
}
