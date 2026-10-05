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
        fun `builds one model per tab whose ChipSet resolves chip keys back to their value`() {
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
            chipsByTab.getValue("all").chips["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
            chipsByTab.getValue("read").chips["readYear:2021"] shouldBe LibraryFilterValue.ReadYear(year = 2021)
        }
    }
}
