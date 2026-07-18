package nl.rhaydus.softcover.core.connectivity.data.sync

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource

class ListWriteReplayTest {
    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private lateinit var replay: ListWriteReplay

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk()
        listsLocalDataSource = mockk()
        replay = ListWriteReplay(
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = listsLocalDataSource,
        )
    }

    private fun pendingListWrite(
        kind: PendingListWriteKind,
        listId: Int? = null,
        listName: String? = null,
        bookId: Int? = null,
        editionId: Int? = null,
        listBookId: Int? = null,
        startPosition: Int? = null,
        orderedListBookIds: List<Int>? = null,
        enqueuedAt: String = "2026-05-10T10:00:00Z",
        privacy: PrivacySetting? = null,
    ) = PendingListWrite(
        kind = kind,
        listId = listId,
        listName = listName,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
        startPosition = startPosition,
        orderedListBookIds = orderedListBookIds,
        enqueuedAt = enqueuedAt,
        privacy = privacy,
    )

    @Nested
    inner class CreateList {
        @Test
        fun `creates the list remotely and caches it locally, returning SYNCED`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.CREATE_LIST,
                listName = "Science Fiction",
            )
            val created = BookList(
                id = 1,
                name = "Science Fiction",
                slug = "science-fiction",
                books = emptyList(),
            )

            coEvery {
                listsRemoteDataSource.createList(name = "Science Fiction")
            } returns created

            coJustRun {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
            }

            // ----- Act -----
            val result = replay(payload = write)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
            }
        }

        @Test
        fun `throws IllegalStateException when listName is missing`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.CREATE_LIST,
                listName = null,
            )

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { replay(payload = write) }
        }

        @Test
        fun `creates the list remotely as PRIVATE when queued with that privacy`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.CREATE_LIST,
                listName = "Science Fiction",
                privacy = PrivacySetting.PRIVATE,
            )
            val created = BookList(
                id = 1,
                name = "Science Fiction",
                slug = "science-fiction",
                books = emptyList(),
            )

            coEvery {
                listsRemoteDataSource.createList(
                    name = "Science Fiction",
                    privacy = PrivacySetting.PRIVATE,
                )
            } returns created

            coJustRun {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
            }

            // ----- Act -----
            val result = replay(payload = write)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                listsRemoteDataSource.createList(
                    name = "Science Fiction",
                    privacy = PrivacySetting.PRIVATE,
                )
            }
        }

        @Test
        fun `falls back to PUBLIC when a row queued before the privacy column existed has no privacy`() =
            runTest {
                // ----- Arrange -----
                val write = pendingListWrite(
                    kind = PendingListWriteKind.CREATE_LIST,
                    listName = "Science Fiction",
                    privacy = null,
                )
                val created = BookList(
                    id = 1,
                    name = "Science Fiction",
                    slug = "science-fiction",
                    books = emptyList(),
                )

                coEvery {
                    listsRemoteDataSource.createList(
                        name = "Science Fiction",
                        privacy = PrivacySetting.PUBLIC,
                    )
                } returns created

                coJustRun {
                    listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
                }

                // ----- Act -----
                val result = replay(payload = write)

                // ----- Assert -----
                result shouldBe ReplayOutcome.SYNCED
                coVerify(exactly = 1) {
                    listsRemoteDataSource.createList(
                        name = "Science Fiction",
                        privacy = PrivacySetting.PUBLIC,
                    )
                }
            }
    }

    @Nested
    inner class AddListBook {
        @Test
        fun `adds the book remotely, clears the optimistic row and caches the real one, returning SYNCED`() =
            runTest {
                // ----- Arrange -----
                val write = pendingListWrite(
                    kind = PendingListWriteKind.ADD_LIST_BOOK,
                    listId = 10,
                    bookId = 20,
                    editionId = 30,
                )
                val real = ListBook(
                    listBookId = 99,
                    listId = 10,
                    bookId = 20,
                    editionId = 30,
                )

                coEvery {
                    listsRemoteDataSource.addBookToList(
                        listId = 10,
                        bookId = 20,
                        editionId = 30,
                    )
                } returns real

                coJustRun {
                    listsLocalDataSource.removeOptimisticListBook(
                        listId = 10,
                        bookId = 20,
                    )
                }

                coJustRun {
                    listsLocalDataSource.cacheListBook(book = real)
                }

                // ----- Act -----
                val result = replay(payload = write)

                // ----- Assert -----
                result shouldBe ReplayOutcome.SYNCED
                coVerify(exactly = 1) {
                    listsLocalDataSource.removeOptimisticListBook(
                        listId = 10,
                        bookId = 20,
                    )
                }
                coVerify(exactly = 1) {
                    listsLocalDataSource.cacheListBook(book = real)
                }
            }
    }

    @Nested
    inner class RemoveListBook {
        @Test
        fun `removes the book remotely and caches the updated list, returning SYNCED`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REMOVE_LIST_BOOK,
                listId = 10,
                bookId = 20,
                editionId = 30,
                listBookId = 40,
            )
            val snapshot = ListBook(
                listBookId = 40,
                listId = 10,
                bookId = 20,
                editionId = 30,
            )
            val updatedList = BookList(
                id = 10,
                name = "My List",
                slug = "my-list",
                books = emptyList(),
            )

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } returns updatedList

            coJustRun {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
            }

            // ----- Act -----
            val result = replay(payload = write)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
            }
        }
    }

    @Nested
    inner class ReorderListBooks {
        @Test
        fun `updates the list book positions remotely and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                listId = 10,
                startPosition = 2,
                orderedListBookIds = listOf(101, 102, 103),
            )

            coJustRun {
                listsRemoteDataSource.updateListBookPositions(
                    listId = 10,
                    startPosition = 2,
                    orderedListBookIds = listOf(101, 102, 103),
                )
            }

            // ----- Act -----
            val result = replay(payload = write)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                listsRemoteDataSource.updateListBookPositions(
                    listId = 10,
                    startPosition = 2,
                    orderedListBookIds = listOf(101, 102, 103),
                )
            }
        }

        @Test
        fun `throws IllegalStateException when orderedListBookIds is empty`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                listId = 10,
                startPosition = 0,
                orderedListBookIds = emptyList(),
            )

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { replay(payload = write) }
        }
    }
}
