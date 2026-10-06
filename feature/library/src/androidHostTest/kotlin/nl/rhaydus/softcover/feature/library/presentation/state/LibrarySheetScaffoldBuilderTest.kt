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
            // ----- Arrange -----
            val resultCount = 7

            // ----- Act -----
            val model = arrangeSheetScaffoldFor(resultCount = resultCount)

            // ----- Assert -----
            val footer = model.footer.shouldNotBeNull()
            footer.primary.shouldNotBeNull().dismissesSheet shouldBe true
        }
    }

    @Nested
    inner class FilterSheetScaffoldFor {
        @Test
        fun `Clear all is present but disabled while no filter is active`() {
            // ----- Arrange -----
            val resultCount = 3
            val filtersActive = false

            // ----- Act -----
            val footer = filterSheetScaffoldFor(
                resultCount = resultCount,
                filtersActive = filtersActive,
            ).footer.shouldNotBeNull()

            // ----- Assert -----
            footer.secondary.shouldNotBeNull().enabled shouldBe false
            footer.secondary.shouldNotBeNull().dismissesSheet shouldBe false
        }

        @Test
        fun `Clear all is enabled once a filter is active`() {
            // ----- Arrange -----
            val resultCount = 3
            val filtersActive = true

            // ----- Act -----
            val footer = filterSheetScaffoldFor(
                resultCount = resultCount,
                filtersActive = filtersActive,
            ).footer.shouldNotBeNull()

            // ----- Assert -----
            footer.secondary.shouldNotBeNull().enabled shouldBe true
        }

        @Test
        fun `primary button dismisses the sheet`() {
            // ----- Arrange -----
            val resultCount = 1
            val filtersActive = true

            // ----- Act -----
            val model = filterSheetScaffoldFor(
                resultCount = resultCount,
                filtersActive = filtersActive,
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
            // ----- Arrange -----
            val resultCount = 1

            // ----- Act -----
            val label = arrangeSheetScaffoldFor(resultCount = resultCount)
                .footer.shouldNotBeNull()
                .primary.shouldNotBeNull()
                .label

            // ----- Assert -----
            label shouldBe "Show 1 title"
        }

        @Test
        fun `filter label pluralises for zero and many results`() {
            // ----- Arrange -----
            val noResults = 0
            val manyResults = 12

            // ----- Act -----
            val none = filterSheetScaffoldFor(
                resultCount = noResults,
                filtersActive = true,
            ).footer.shouldNotBeNull().primary.shouldNotBeNull().label
            val many = filterSheetScaffoldFor(
                resultCount = manyResults,
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
            // ----- Arrange -----
            val bookCount = 3
            val inProgress = false

            // ----- Act -----
            val model = bulkRemoveSheetScaffoldFor(
                bookCount = bookCount,
                inProgress = inProgress,
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
            // ----- Arrange -----
            val bookCount = 3
            val inProgress = true

            // ----- Act -----
            val model = bulkRemoveSheetScaffoldFor(
                bookCount = bookCount,
                inProgress = inProgress,
            )

            // ----- Assert -----
            val footer = model.footer.shouldNotBeNull()
            model.busy shouldBe true
            footer.primary.shouldNotBeNull().enabled shouldBe true
            footer.secondary.shouldNotBeNull().enabled shouldBe true
        }

        @Test
        fun `headline carries the count and singularises for one book`() {
            // ----- Arrange -----
            val oneBook = 1
            val manyBooks = 4

            // ----- Act -----
            val one = bulkRemoveSheetScaffoldFor(
                bookCount = oneBook,
                inProgress = false,
            )
            val many = bulkRemoveSheetScaffoldFor(
                bookCount = manyBooks,
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
            // ----- Arrange -----
            val visibleTabs = listOf(allTab, readingTab, listTab)
            val selectedTabId = allTab.id
            val tabStatsByTab = emptyMap<String, LibraryTabStats>()

            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = visibleTabs,
                selectedTabId = selectedTabId,
                tabStatsByTab = tabStatsByTab,
            )

            // ----- Assert -----
            rows.map { it.key } shouldBe listOf(allTab.id, readingTab.id, listTab.id)
            rows.map { it.label } shouldBe listOf(allTab.label, readingTab.label, listTab.label)
        }

        @Test
        fun `only the selected tab's row is selected`() {
            // ----- Arrange -----
            val visibleTabs = listOf(allTab, readingTab)
            val selectedTabId = readingTab.id
            val tabStatsByTab = emptyMap<String, LibraryTabStats>()

            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = visibleTabs,
                selectedTabId = selectedTabId,
                tabStatsByTab = tabStatsByTab,
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(false, true)
        }

        @Test
        fun `count comes from the tab stats and falls back to zero when absent`() {
            // ----- Arrange -----
            val visibleTabs = listOf(allTab, readingTab)
            val selectedTabId = allTab.id
            val tabStatsByTab = mapOf(
                allTab.id to LibraryTabStats(
                    itemCount = 12,
                    totalPages = 300,
                ),
            )

            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = visibleTabs,
                selectedTabId = selectedTabId,
                tabStatsByTab = tabStatsByTab,
            )

            // ----- Assert -----
            rows.map { it.count?.text } shouldBe listOf("12", "0")
        }

        @Test
        fun `count description spells the count out as titles and singularises for one`() {
            // ----- Arrange -----
            val visibleTabs = listOf(allTab, readingTab, listTab)
            val selectedTabId = allTab.id
            val tabStatsByTab = mapOf(
                allTab.id to LibraryTabStats(
                    itemCount = 12,
                    totalPages = 0,
                ),
                readingTab.id to LibraryTabStats(
                    itemCount = 1,
                    totalPages = 0,
                ),
            )

            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = visibleTabs,
                selectedTabId = selectedTabId,
                tabStatsByTab = tabStatsByTab,
            )

            // ----- Assert -----
            rows.map { it.count?.description } shouldBe listOf("12 titles", "1 title", "0 titles")
        }

        @Test
        fun `custom lists get a different leading icon from shelves`() {
            // ----- Arrange -----
            val visibleTabs = listOf(allTab, readingTab, listTab)
            val selectedTabId = allTab.id
            val tabStatsByTab = emptyMap<String, LibraryTabStats>()

            // ----- Act -----
            val rows = shelvesSheetRowsFor(
                visibleTabs = visibleTabs,
                selectedTabId = selectedTabId,
                tabStatsByTab = tabStatsByTab,
            )

            // ----- Assert -----
            rows[0].leadingIcon shouldBe rows[1].leadingIcon
            (rows[2].leadingIcon == rows[0].leadingIcon) shouldBe false
        }
    }
}
