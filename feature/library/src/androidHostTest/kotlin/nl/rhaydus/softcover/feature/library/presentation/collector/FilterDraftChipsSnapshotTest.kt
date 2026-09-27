package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FilterDraftChipsSnapshotTest {
    @Nested
    inner class Compute {
        @Test
        fun `compute returns null while the draft is null (sheet closed)`() {
            // ----- Arrange -----
            val snapshot = FilterDraftChipsSnapshot(
                draft = null,
                filterChipsByTab = emptyMap(),
                filterValueByChipKey = emptyMap(),
                booksByTab = emptyMap(),
                editionsByTab = emptyMap(),
                addedAtByTab = emptyMap(),
                bookByBookId = emptyMap(),
                sortModeByTab = emptyMap(),
                sortDirectionByTab = emptyMap(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection shouldBe null
        }

        @Test
        fun `compute falls back to an empty LibraryFilterChips when the draft's tab has no entry in filterChipsByTab`() {
            // ----- Arrange -----
            val snapshot = FilterDraftChipsSnapshot(
                draft = LibraryFilterDraft(tabId = "all"),
                filterChipsByTab = emptyMap(),
                filterValueByChipKey = emptyMap(),
                booksByTab = emptyMap(),
                editionsByTab = emptyMap(),
                addedAtByTab = emptyMap(),
                bookByBookId = emptyMap(),
                sortModeByTab = emptyMap(),
                sortDirectionByTab = emptyMap(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.ownershipChips shouldBe emptyList()
            selection?.resultCount shouldBe 0
        }

        @Test
        fun `compute resolves chips for the draft's own tab out of filterChipsByTab`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                formatChips = listOf(ChipUiModel(
                    key = "format:ebook",
                    label = "ebook",
                ),),
            )
            val snapshot = FilterDraftChipsSnapshot(
                draft = LibraryFilterDraft(tabId = "read"),
                filterChipsByTab = mapOf(
                    "all" to LibraryFilterChips(),
                    "read" to chips,
                ),
                filterValueByChipKey = emptyMap(),
                booksByTab = emptyMap(),
                editionsByTab = emptyMap(),
                addedAtByTab = emptyMap(),
                bookByBookId = emptyMap(),
                sortModeByTab = emptyMap(),
                sortDirectionByTab = emptyMap(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.formatChips?.map { it.key } shouldBe listOf("format:ebook")
        }

        @Test
        fun `compute counts only the draft's tab entry in booksByTab when its filters are empty`() {
            // ----- Arrange -----
            val snapshot = FilterDraftChipsSnapshot(
                draft = LibraryFilterDraft(tabId = "all"),
                filterChipsByTab = emptyMap(),
                filterValueByChipKey = emptyMap(),
                booksByTab = mapOf("all" to List(3) { mockk() }),
                editionsByTab = emptyMap(),
                addedAtByTab = emptyMap(),
                bookByBookId = emptyMap(),
                sortModeByTab = emptyMap(),
                sortDirectionByTab = emptyMap(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.resultCount shouldBe 3
        }

        @Test
        fun `compute derives resultCount from the draft's own uncommitted filters, not an empty default`() {
            // ----- Arrange -----
            val matchingBook = mockk<Book>(relaxed = true).also {
                every {
                    it.releaseYear
                } returns 2020
            }
            val nonMatchingBook = mockk<Book>(relaxed = true).also {
                every {
                    it.releaseYear
                } returns 2019
            }
            val snapshot = FilterDraftChipsSnapshot(
                draft = LibraryFilterDraft(
                    tabId = "all",
                    filters = LibraryFilters(releaseYears = setOf(2020)),
                ),
                filterChipsByTab = emptyMap(),
                filterValueByChipKey = emptyMap(),
                booksByTab = mapOf("all" to listOf(matchingBook, nonMatchingBook)),
                editionsByTab = emptyMap(),
                addedAtByTab = emptyMap(),
                bookByBookId = emptyMap(),
                sortModeByTab = emptyMap(),
                sortDirectionByTab = emptyMap(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.resultCount shouldBe 1
        }
    }
}
