package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTagOption
import nl.rhaydus.softcover.feature.library.presentation.state.TagSelection
import nl.rhaydus.softcover.feature.library.presentation.state.UserTagKey
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
                filterChipsByTab = persistentMapOf(),
                filterOptionsByTab = persistentMapOf(),
                booksByTab = persistentMapOf(),
                editionsByTab = persistentMapOf(),
                addedAtByTab = persistentMapOf(),
                bookByBookId = persistentMapOf(),
                userTagsByBookId = persistentMapOf(),
                sortModeByTab = persistentMapOf(),
                sortDirectionByTab = persistentMapOf(),
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
                filterChipsByTab = persistentMapOf(),
                filterOptionsByTab = persistentMapOf(),
                booksByTab = persistentMapOf(),
                editionsByTab = persistentMapOf(),
                addedAtByTab = persistentMapOf(),
                bookByBookId = persistentMapOf(),
                userTagsByBookId = persistentMapOf(),
                sortModeByTab = persistentMapOf(),
                sortDirectionByTab = persistentMapOf(),
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
            val ebookValue = LibraryFilterValue.Format(value = "ebook")
            val chips = LibraryFilterChips(
                formatChips = listOf(
                    ChipUiModel(
                        key = "format:ebook",
                        label = "ebook",
                    ) to ebookValue,
                ).toChipSet(),
            )
            val snapshot = FilterDraftChipsSnapshot(
                draft = LibraryFilterDraft(tabId = "read"),
                filterChipsByTab = persistentMapOf(
                    "all" to LibraryFilterChips(),
                    "read" to chips,
                ),
                filterOptionsByTab = persistentMapOf(),
                booksByTab = persistentMapOf(),
                editionsByTab = persistentMapOf(),
                addedAtByTab = persistentMapOf(),
                bookByBookId = persistentMapOf(),
                userTagsByBookId = persistentMapOf(),
                sortModeByTab = persistentMapOf(),
                sortDirectionByTab = persistentMapOf(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.formatChips?.map { it.key } shouldBe listOf("format:ebook")
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
                    filters = LibraryFilters(releaseYears = persistentSetOf(2020)),
                ),
                filterChipsByTab = persistentMapOf(),
                filterOptionsByTab = persistentMapOf(),
                booksByTab = persistentMapOf("all" to persistentListOf(matchingBook, nonMatchingBook)),
                editionsByTab = persistentMapOf(),
                addedAtByTab = persistentMapOf(),
                bookByBookId = persistentMapOf(),
                userTagsByBookId = persistentMapOf(),
                sortModeByTab = persistentMapOf(),
                sortDirectionByTab = persistentMapOf(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.resultCount shouldBe 1
        }

        @Test
        fun `compute counts the books a draft Yours filter would keep, using the user's own tags`() {
            // ----- Arrange -----
            val taggedBook = mockk<Book>(relaxed = true).also {
                every {
                    it.id
                } returns 1
            }
            val untaggedBook = mockk<Book>(relaxed = true).also {
                every {
                    it.id
                } returns 2
            }
            val cozyKey = UserTagKey(
                category = TagCategory.MOOD,
                name = "cozy",
            )
            val snapshot = FilterDraftChipsSnapshot(
                draft = LibraryFilterDraft(
                    tabId = "all",
                    filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(cozyKey))),
                ),
                filterChipsByTab = persistentMapOf(),
                filterOptionsByTab = persistentMapOf(),
                booksByTab = persistentMapOf("all" to persistentListOf(taggedBook, untaggedBook)),
                editionsByTab = persistentMapOf(),
                addedAtByTab = persistentMapOf(),
                bookByBookId = persistentMapOf(),
                userTagsByBookId = persistentMapOf(
                    1 to listOf(
                        UserTag(
                            name = "cozy",
                            category = TagCategory.MOOD,
                        ),
                    ),
                ),
                sortModeByTab = persistentMapOf(),
                sortDirectionByTab = persistentMapOf(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.resultCount shouldBe 1
        }

        @Test
        fun `compute builds the tag summaries from the draft's own tab's options`() {
            // ----- Arrange -----
            val cozyKey = UserTagKey(
                category = TagCategory.MOOD,
                name = "cozy",
            )
            val snapshot = FilterDraftChipsSnapshot(
                draft = LibraryFilterDraft(tabId = "read"),
                filterChipsByTab = persistentMapOf(),
                filterOptionsByTab = persistentMapOf(
                    "all" to LibraryFilterOptions(),
                    "read" to LibraryFilterOptions(
                        yourTags = persistentListOf(
                            LibraryTagOption(
                                key = cozyKey,
                                category = TagCategory.MOOD,
                                count = 4,
                            ),
                        ),
                    ),
                ),
                booksByTab = persistentMapOf(),
                editionsByTab = persistentMapOf(),
                addedAtByTab = persistentMapOf(),
                bookByBookId = persistentMapOf(),
                userTagsByBookId = persistentMapOf(),
                sortModeByTab = persistentMapOf(),
                sortDirectionByTab = persistentMapOf(),
                searchQuery = "",
            )

            // ----- Act -----
            val selection = snapshot.compute()

            // ----- Assert -----
            selection?.yourTags?.tagChips?.map { it.label } shouldBe listOf("cozy · 4")
        }
    }
}
