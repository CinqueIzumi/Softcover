package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.collector.shelvesSheetRowsFor
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibrarySheetScaffoldBuilderTest {
    @Nested
    inner class ArrangeSheetScaffoldFor {
        @Test
        fun `primary button dismisses the sheet`() {
            // ----- Act -----
            val model = arrangeSheetScaffoldFor(resultCount = 7)

            // ----- Assert -----
            val footer = model.footer.shouldNotBeNull()
            footer.primary.shouldNotBeNull().dismissesSheet shouldBe true
        }
    }

    @Nested
    inner class FilterSheetScaffoldFor {
        @Test
        fun `Clear all is present but disabled while no filter is active`() {
            // ----- Act -----
            val footer = filterSheetScaffoldFor(
                resultCount = 3,
                filtersActive = false,
            ).footer.shouldNotBeNull()

            // ----- Assert -----
            footer.secondary.shouldNotBeNull().enabled shouldBe false
            footer.secondary.shouldNotBeNull().dismissesSheet shouldBe false
        }

        @Test
        fun `Clear all is enabled once a filter is active`() {
            // ----- Act -----
            val footer = filterSheetScaffoldFor(
                resultCount = 3,
                filtersActive = true,
            ).footer.shouldNotBeNull()

            // ----- Assert -----
            footer.secondary.shouldNotBeNull().enabled shouldBe true
        }

        @Test
        fun `primary button dismisses the sheet`() {
            // ----- Act -----
            val model = filterSheetScaffoldFor(
                resultCount = 1,
                filtersActive = true,
            )

            // ----- Assert -----
            val footer = model.footer.shouldNotBeNull()
            footer.primary.shouldNotBeNull().dismissesSheet shouldBe true
        }
    }

    @Nested
    inner class ShowResultsButton {
        @Test
        fun `arrange label singularises for one result`() {
            // ----- Act -----
            val label = arrangeSheetScaffoldFor(resultCount = 1).footer.shouldNotBeNull().primary.shouldNotBeNull().label

            // ----- Assert -----
            label shouldBe "Show 1 title"
        }

        @Test
        fun `filter label pluralises for zero and many results`() {
            // ----- Act -----
            val none = filterSheetScaffoldFor(
                resultCount = 0,
                filtersActive = true,
            ).footer.shouldNotBeNull().primary.shouldNotBeNull().label
            val many = filterSheetScaffoldFor(
                resultCount = 12,
                filtersActive = true,
            ).footer.shouldNotBeNull().primary.shouldNotBeNull().label

            // ----- Assert -----
            none shouldBe "Show 0 titles"
            many shouldBe "Show 12 titles"
        }
    }

    @Nested
    inner class BulkRemoveSheetScaffoldFor {
        @Test
        fun `idle is not busy, Remove stays in the sheet and Cancel dismisses it`() {
            // ----- Act -----
            val model = bulkRemoveSheetScaffoldFor(
                bookCount = 3,
                inProgress = false,
            )

            // ----- Assert -----
            val footer = model.footer.shouldNotBeNull()
            model.busy shouldBe false
            footer.primary.shouldNotBeNull().dismissesSheet shouldBe false
            footer.secondary.shouldNotBeNull().dismissesSheet shouldBe true
            model.closeRequested shouldBe false
        }

        @Test
        fun `in progress marks the whole sheet busy rather than touching individual buttons`() {
            // ----- Act -----
            val model = bulkRemoveSheetScaffoldFor(
                bookCount = 3,
                inProgress = true,
            )

            // ----- Assert -----
            val footer = model.footer.shouldNotBeNull()
            model.busy shouldBe true
            footer.primary.shouldNotBeNull().enabled shouldBe true
            footer.secondary.shouldNotBeNull().enabled shouldBe true
        }

        @Test
        fun `headline carries the count and singularises for one book`() {
            // ----- Act -----
            val one = bulkRemoveSheetScaffoldFor(
                bookCount = 1,
                inProgress = false,
            )
            val many = bulkRemoveSheetScaffoldFor(
                bookCount = 4,
                inProgress = false,
            )

            // ----- Assert -----
            one.header.section.headlineText shouldBe "Remove 1 book?"
            many.header.section.headlineText shouldBe "Remove 4 books?"
        }
    }

    @Nested
    inner class ShelvesSheetRowsFor {
        private val allTab = LibraryTab.All
        private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)
        private val listTab = LibraryTab.CustomList(
            listId = 9,
            listName = "Favourites",
        )

        @Test
        fun `maps each visible tab to a row keyed by its id, in order`() {
            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = listOf(allTab, readingTab, listTab),
                selectedTabId = allTab.id,
                tabStatsByTab = emptyMap(),
            )

            // ----- Assert -----
            rows.map { it.key } shouldBe listOf(allTab.id, readingTab.id, listTab.id)
            rows.map { it.label } shouldBe listOf(allTab.label, readingTab.label, listTab.label)
        }

        @Test
        fun `only the selected tab's row is selected`() {
            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = listOf(allTab, readingTab),
                selectedTabId = readingTab.id,
                tabStatsByTab = emptyMap(),
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(false, true)
        }

        @Test
        fun `count comes from the tab stats and falls back to zero when absent`() {
            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = listOf(allTab, readingTab),
                selectedTabId = allTab.id,
                tabStatsByTab = mapOf(
                    allTab.id to LibraryTabStats(
                        itemCount = 12,
                        totalPages = 300,
                    ),
                ),
            )

            // ----- Assert -----
            rows.map { it.count?.text } shouldBe listOf("12", "0")
        }

        @Test
        fun `count description spells the count out as titles and singularises for one`() {
            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = listOf(allTab, readingTab, listTab),
                selectedTabId = allTab.id,
                tabStatsByTab = mapOf(
                    allTab.id to LibraryTabStats(
                        itemCount = 12,
                        totalPages = 0,
                    ),
                    readingTab.id to LibraryTabStats(
                        itemCount = 1,
                        totalPages = 0,
                    ),
                ),
            )

            // ----- Assert -----
            rows.map { it.count?.description } shouldBe listOf("12 titles", "1 title", "0 titles")
        }

        @Test
        fun `custom lists get a different leading icon from shelves`() {
            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = listOf(allTab, readingTab, listTab),
                selectedTabId = allTab.id,
                tabStatsByTab = emptyMap(),
            )

            // ----- Assert -----
            rows[0].leadingIcon shouldBe rows[1].leadingIcon
            (rows[2].leadingIcon == rows[0].leadingIcon) shouldBe false
        }
    }
}
