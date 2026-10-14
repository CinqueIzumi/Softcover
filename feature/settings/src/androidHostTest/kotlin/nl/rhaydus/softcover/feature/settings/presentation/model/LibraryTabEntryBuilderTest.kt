package nl.rhaydus.softcover.feature.settings.presentation.model

import io.kotest.inspectors.forAll
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.withOrderedEntries
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTabEntryBuilderTest {
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

    private fun build(
        statusCounts: Map<Int, Int> = emptyMap(),
        totalCount: Int = 0,
        availableLists: List<BookList> = emptyList(),
        draftEnabledStatusCodes: Set<Int> = emptySet(),
        draftEnabledListIds: Set<Int> = emptySet(),
        draftTabOrder: List<String> = emptyList(),
    ): List<LibraryTabEntry> = buildLibraryTabEntries(
        statusCounts = statusCounts,
        totalCount = totalCount,
        availableLists = availableLists,
        draftEnabledStatusCodes = draftEnabledStatusCodes,
        draftEnabledListIds = draftEnabledListIds,
        draftTabOrder = draftTabOrder,
    )

    @Nested
    inner class DefaultOrder {
        @Test
        fun `orders All, the statuses, then lists sorted case-insensitively by name without a draft order`() {
            // ----- Arrange -----
            val lists = listOf(
                stubBookList(
                    id = 3,
                    name = "Zebra",
                ),
                stubBookList(
                    id = 1,
                    name = "alpha",
                ),
                stubBookList(
                    id = 2,
                    name = "Middle",
                ),
            )

            // ----- Act -----
            val entries = build(availableLists = lists)

            // ----- Assert -----
            entries.map { it::class } shouldBe listOf(
                LibraryTabEntry.All::class,
                LibraryTabEntry.Status::class,
                LibraryTabEntry.Status::class,
                LibraryTabEntry.Status::class,
                LibraryTabEntry.Status::class,
                LibraryTabEntry.Status::class,
                LibraryTabEntry.CustomList::class,
                LibraryTabEntry.CustomList::class,
                LibraryTabEntry.CustomList::class,
            )
            entries.filterIsInstance<LibraryTabEntry.Status>().map { it.status } shouldBe listOf(
                UserBookStatus.CURRENTLY_READING,
                UserBookStatus.PAUSED,
                UserBookStatus.WANT_TO_READ,
                UserBookStatus.READ,
                UserBookStatus.DID_NOT_FINISH,
            )
            entries.filterIsInstance<LibraryTabEntry.CustomList>().map { it.listName } shouldBe listOf(
                "alpha",
                "Middle",
                "Zebra",
            )
        }
    }

    @Nested
    inner class DraftTabOrder {
        @Test
        fun `known ids reorder entries accordingly`() {
            // ----- Arrange -----
            val lists = listOf(
                stubBookList(
                    id = 1,
                    name = "Alpha",
                ),
                stubBookList(
                    id = 2,
                    name = "Beta",
                ),
            )
            val order = listOf(
                "list-2",
                "status-${UserBookStatus.READ.code}",
                "list-1",
            )

            // ----- Act -----
            val entries = build(
                availableLists = lists,
                draftTabOrder = order,
            )

            // ----- Assert -----
            entries[0].shouldBeInstanceOf<LibraryTabEntry.All>()
            (entries[1] as LibraryTabEntry.CustomList).listId shouldBe 2
            (entries[2] as LibraryTabEntry.Status).status shouldBe UserBookStatus.READ
            (entries[3] as LibraryTabEntry.CustomList).listId shouldBe 1
        }

        @Test
        fun `unknown ids are ignored`() {
            // ----- Arrange -----
            val unknownIds = listOf(
                "unknown-id-1",
                "unknown-id-2",
            )

            // ----- Act -----
            val entries = build(draftTabOrder = unknownIds)

            // ----- Assert -----
            entries.size shouldBe 6
            entries.none { it.id == "unknown-id-1" } shouldBe true
            entries.none { it.id == "unknown-id-2" } shouldBe true
        }

        @Test
        fun `entries missing from the order are appended in default order`() {
            // ----- Arrange -----
            val lists = listOf(
                stubBookList(
                    id = 1,
                    name = "Alpha",
                ),
                stubBookList(
                    id = 2,
                    name = "Beta",
                ),
            )

            // ----- Act -----
            val entries = build(
                availableLists = lists,
                draftTabOrder = listOf("list-2"),
            )

            // ----- Assert -----
            entries[1].id shouldBe "list-2"
            entries.filterIsInstance<LibraryTabEntry.CustomList>().map { it.listId } shouldBe listOf(
                2,
                1,
            )
            entries.size shouldBe 8
        }

        @Test
        fun `a duplicate id in the order yields that entry once in its first position`() {
            // ----- Arrange -----
            val lists = listOf(
                stubBookList(
                    id = 1,
                    name = "Alpha",
                ),
                stubBookList(
                    id = 2,
                    name = "Beta",
                ),
            )
            val order = listOf(
                "list-2",
                "list-1",
                "list-2",
            )

            // ----- Act -----
            val entries = build(
                availableLists = lists,
                draftTabOrder = order,
            )

            // ----- Assert -----
            entries.count { it.id == "list-2" } shouldBe 1
            entries[1].id shouldBe "list-2"
            entries[2].id shouldBe "list-1"
            entries.size shouldBe 8
        }
    }

    @Nested
    inner class Counts {
        @Test
        fun `count, isReorderable, canHide and isList are populated correctly per entry kind`() {
            // ----- Arrange -----
            val statusCounts = mapOf(
                UserBookStatus.CURRENTLY_READING.code to 3,
                UserBookStatus.PAUSED.code to 6,
                UserBookStatus.WANT_TO_READ.code to 5,
                UserBookStatus.READ.code to 11,
                UserBookStatus.DID_NOT_FINISH.code to 2,
            )
            val list = stubBookList(
                id = 1,
                name = "MyList",
                bookCount = 7,
            )

            // ----- Act -----
            val entries = build(
                availableLists = listOf(list),
                statusCounts = statusCounts,
                totalCount = 21,
            )

            // ----- Assert -----
            val allEntry = entries[0] as LibraryTabEntry.All
            allEntry.isReorderable shouldBe false
            allEntry.canHide shouldBe false
            allEntry.isList shouldBe false
            allEntry.count shouldBe 21

            val statuses = entries.filterIsInstance<LibraryTabEntry.Status>()
            statuses.map { it.count } shouldBe listOf(
                3,
                6,
                5,
                11,
                2,
            )
            statuses.forAll { it.isReorderable shouldBe true }
            statuses.filterNot { it.status == UserBookStatus.CURRENTLY_READING }.forAll { it.canHide shouldBe true }
            statuses.first { it.status == UserBookStatus.CURRENTLY_READING }.canHide shouldBe false

            val listEntry = entries.filterIsInstance<LibraryTabEntry.CustomList>().single()
            listEntry.isList shouldBe true
            listEntry.canHide shouldBe true
            listEntry.count shouldBe 7
        }

        @Test
        fun `status missing from statusCounts counts as zero`() {
            // ----- Arrange -----
            val statusCounts = mapOf(UserBookStatus.READ.code to 4)

            // ----- Act -----
            val entries = build(statusCounts = statusCounts)

            // ----- Assert -----
            entries
                .filterIsInstance<LibraryTabEntry.Status>()
                .associate { it.status to it.count } shouldBe mapOf(
                UserBookStatus.CURRENTLY_READING to 0,
                UserBookStatus.PAUSED to 0,
                UserBookStatus.WANT_TO_READ to 0,
                UserBookStatus.READ to 4,
                UserBookStatus.DID_NOT_FINISH to 0,
            )
        }
    }

    @Nested
    inner class Toggles {
        @Test
        fun `status toggle checked follows draftEnabledStatusCodes`() {
            // ----- Arrange -----
            val enabledCodes = setOf(UserBookStatus.READ.code)

            // ----- Act -----
            val entries = build(draftEnabledStatusCodes = enabledCodes)

            // ----- Assert -----
            val byStatus = entries.filterIsInstance<LibraryTabEntry.Status>().associateBy { it.status }
            byStatus.getValue(UserBookStatus.READ).statusVisibilityToggle.shouldNotBeNull().checked shouldBe true
            byStatus.getValue(UserBookStatus.WANT_TO_READ).statusVisibilityToggle.shouldNotBeNull().checked shouldBe false
            byStatus.getValue(UserBookStatus.DID_NOT_FINISH).statusVisibilityToggle.shouldNotBeNull().checked shouldBe false
        }

        @Test
        fun `entries that cannot be hidden have no toggle even when their code is enabled`() {
            // ----- Arrange -----
            val enabledCodes = setOf(UserBookStatus.CURRENTLY_READING.code)

            // ----- Act -----
            val entries = build(draftEnabledStatusCodes = enabledCodes)

            // ----- Assert -----
            val currentlyReading = entries
                .filterIsInstance<LibraryTabEntry.Status>()
                .single { it.status == UserBookStatus.CURRENTLY_READING }
            currentlyReading.statusVisibilityToggle.shouldBeNull()
            entries[0].visibilityToggle.shouldBeNull()
        }

        @Test
        fun `list toggle checked follows draftEnabledListIds`() {
            // ----- Arrange -----
            val lists = listOf(
                stubBookList(
                    id = 1,
                    name = "Alpha",
                ),
                stubBookList(
                    id = 2,
                    name = "Beta",
                ),
            )

            // ----- Act -----
            val entries = build(
                availableLists = lists,
                draftEnabledListIds = setOf(2),
            )

            // ----- Assert -----
            val byId = entries.filterIsInstance<LibraryTabEntry.CustomList>().associateBy { it.listId }
            byId.getValue(1).listVisibilityToggle.checked shouldBe false
            byId.getValue(2).listVisibilityToggle.checked shouldBe true
        }
    }

    @Nested
    inner class WithOrderedEntries {
        @Test
        fun `fills orderedEntries from the state inputs`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                totalCount = 8,
                draftTabOrder = persistentListOf("status-${UserBookStatus.READ.code}"),
            )

            // ----- Act -----
            val result = state.withOrderedEntries()

            // ----- Assert -----
            result.orderedEntries[0].count shouldBe 8
            result.orderedEntries[1].id shouldBe "status-${UserBookStatus.READ.code}"
        }
    }
}
