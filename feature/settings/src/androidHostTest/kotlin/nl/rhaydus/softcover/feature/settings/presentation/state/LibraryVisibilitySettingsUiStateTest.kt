package nl.rhaydus.softcover.feature.settings.presentation.state

import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry

class LibraryVisibilitySettingsUiStateTest {
    private fun stubBookList(
        id: Int,
        name: String,
        bookCount: Int = 0,
    ): BookList = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.name
        } returns name
        every {
            this@mockk.books
        } returns List(bookCount) { mockk() }
    }

    @Nested
    inner class OrderedEntries {
        @Test
        fun `returns default status order followed by lists sorted by name when draftTabOrder is empty`() {
            // ----- Arrange -----
            val listZ = stubBookList(
                id = 3,
                name = "Zebra",
            )
            val listA = stubBookList(
                id = 1,
                name = "Alpha",
            )
            val listM = stubBookList(
                id = 2,
                name = "Middle",
            )

            val state = LibraryVisibilitySettingsUiState(
                availableLists = listOf(listZ, listA, listM),
                draftTabOrder = emptyList(),
            )

            // ----- Act -----
            val entries = state.orderedEntries

            // ----- Assert -----
            val statusEntries = entries.filterIsInstance<LibraryTabEntry.Status>()
            statusEntries.map { it.status } shouldBe listOf(
                UserBookStatus.CURRENTLY_READING,
                UserBookStatus.WANT_TO_READ,
                UserBookStatus.READ,
                UserBookStatus.DID_NOT_FINISH,
            )

            val listEntries = entries.filterIsInstance<LibraryTabEntry.CustomList>()
            listEntries.map { it.listName } shouldBe listOf("Alpha", "Middle", "Zebra")
        }

        @Test
        fun `status entries always precede list entries in default order`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                availableLists = listOf(stubBookList(
                    id = 1,
                    name = "MyList",
                ),),
                draftTabOrder = emptyList(),
            )

            // ----- Act -----
            val entries = state.orderedEntries

            // ----- Assert -----
            val firstListIndex = entries.indexOfFirst { it is LibraryTabEntry.CustomList }
            val lastStatusIndex = entries.indexOfLast { it is LibraryTabEntry.Status }
            (lastStatusIndex < firstListIndex) shouldBe true
        }

        @Test
        fun `draftTabOrder with known ids reorders entries accordingly`() {
            // ----- Arrange -----
            val listA = stubBookList(
                id = 1,
                name = "Alpha",
            )
            val listB = stubBookList(
                id = 2,
                name = "Beta",
            )
            val readId = "status-${UserBookStatus.READ.code}"
            val listBId = "list-2"
            val listAId = "list-1"

            val state = LibraryVisibilitySettingsUiState(
                availableLists = listOf(listA, listB),
                draftTabOrder = listOf(listBId, readId, listAId),
            )

            // ----- Act -----
            val entries = state.orderedEntries

            // ----- Assert -----
            entries[0].shouldBeInstanceOf<LibraryTabEntry.All>()
            (entries[1] as LibraryTabEntry.CustomList).listId shouldBe 2
            (entries[2] as LibraryTabEntry.Status).status shouldBe UserBookStatus.READ
            (entries[3] as LibraryTabEntry.CustomList).listId shouldBe 1
        }

        @Test
        fun `unknown ids in draftTabOrder are ignored`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                availableLists = emptyList(),
                draftTabOrder = listOf("unknown-id-1", "unknown-id-2"),
            )

            // ----- Act -----
            val entries = state.orderedEntries

            // ----- Assert -----
            entries.none { it.id == "unknown-id-1" } shouldBe true
            entries.none { it.id == "unknown-id-2" } shouldBe true
        }

        @Test
        fun `entries not in draftTabOrder are appended at end in default order`() {
            // ----- Arrange -----
            val listA = stubBookList(
                id = 1,
                name = "Alpha",
            )
            val listB = stubBookList(
                id = 2,
                name = "Beta",
            )
            val listBId = "list-2"

            val state = LibraryVisibilitySettingsUiState(
                availableLists = listOf(listA, listB),
                draftTabOrder = listOf(listBId),
            )

            // ----- Act -----
            val entries = state.orderedEntries

            // ----- Assert -----
            val listEntries = entries.filterIsInstance<LibraryTabEntry.CustomList>()
            listEntries[0].listId shouldBe 2
            listEntries[1].listId shouldBe 1
        }

        @Test
        fun `first entry is always All with count equal to totalCount`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                totalCount = 42,
            )

            // ----- Act -----
            val entries = state.orderedEntries

            // ----- Assert -----
            entries[0].shouldBeInstanceOf<LibraryTabEntry.All>()
            entries[0].count shouldBe 42
        }

        @Test
        fun `count, isReorderable, canHide and isList are populated correctly per entry kind`() {
            // ----- Arrange -----
            val list = stubBookList(
                id = 1,
                name = "MyList",
                bookCount = 7,
            )
            val statusCounts = mapOf(
                UserBookStatus.CURRENTLY_READING.code to 3,
                UserBookStatus.WANT_TO_READ.code to 5,
                UserBookStatus.READ.code to 11,
                UserBookStatus.DID_NOT_FINISH.code to 2,
            )
            val state = LibraryVisibilitySettingsUiState(
                availableLists = listOf(list),
                statusCounts = statusCounts,
                totalCount = 21,
            )

            // ----- Act -----
            val entries = state.orderedEntries

            // ----- Assert -----
            val allEntry = entries[0] as LibraryTabEntry.All
            allEntry.isReorderable shouldBe false
            allEntry.canHide shouldBe false
            allEntry.isList shouldBe false
            allEntry.count shouldBe 21

            val currentlyReadingEntry = entries
                .filterIsInstance<LibraryTabEntry.Status>()
                .first { it.status == UserBookStatus.CURRENTLY_READING }
            currentlyReadingEntry.isReorderable shouldBe true
            currentlyReadingEntry.canHide shouldBe false
            currentlyReadingEntry.count shouldBe 3

            val otherStatusEntries = entries
                .filterIsInstance<LibraryTabEntry.Status>()
                .filterNot { it.status == UserBookStatus.CURRENTLY_READING }
            otherStatusEntries.forAll { entry ->
                entry.isReorderable shouldBe true
                entry.canHide shouldBe true
            }
            otherStatusEntries.first { it.status == UserBookStatus.WANT_TO_READ }.count shouldBe 5
            otherStatusEntries.first { it.status == UserBookStatus.READ }.count shouldBe 11
            otherStatusEntries.first { it.status == UserBookStatus.DID_NOT_FINISH }.count shouldBe 2

            val listEntry = entries.filterIsInstance<LibraryTabEntry.CustomList>().first()
            listEntry.isList shouldBe true
            listEntry.canHide shouldBe true
            listEntry.count shouldBe 7
        }
    }

    @Nested
    inner class IsDirty {
        @Test
        fun `is false when not initialized`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = false,
                persistedEnabledStatusCodes = setOf(1),
                draftEnabledStatusCodes = setOf(99),
            )

            // ----- Assert -----
            state.isDirty shouldBe false
        }

        @Test
        fun `is false when initialized and draft matches persisted across all three fields`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = setOf(1, 2),
                draftEnabledStatusCodes = setOf(1, 2),
                persistedEnabledListIds = setOf(10),
                draftEnabledListIds = setOf(10),
                persistedTabOrder = listOf("list-1", "status-2"),
                draftTabOrder = listOf("list-1", "status-2"),
            )

            // ----- Assert -----
            state.isDirty shouldBe false
        }

        @Test
        fun `is true when only draftTabOrder differs from persistedTabOrder`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = setOf(1),
                draftEnabledStatusCodes = setOf(1),
                persistedEnabledListIds = setOf(10),
                draftEnabledListIds = setOf(10),
                persistedTabOrder = listOf("status-1", "list-10"),
                draftTabOrder = listOf("list-10", "status-1"),
            )

            // ----- Assert -----
            state.isDirty shouldBe true
        }

        @Test
        fun `is true when draftEnabledStatusCodes differs from persisted`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = setOf(1),
                draftEnabledStatusCodes = setOf(1, 3),
                persistedEnabledListIds = emptySet(),
                draftEnabledListIds = emptySet(),
                persistedTabOrder = emptyList(),
                draftTabOrder = emptyList(),
            )

            // ----- Assert -----
            state.isDirty shouldBe true
        }

        @Test
        fun `is true when draftEnabledListIds differs from persisted`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = setOf(5),
                draftEnabledListIds = setOf(5, 7),
                persistedTabOrder = emptyList(),
                draftTabOrder = emptyList(),
            )

            // ----- Assert -----
            state.isDirty shouldBe true
        }
    }
}
