package nl.rhaydus.softcover.core.book.data.sync

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.baseBook
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.sync.UserBookCacheGate.RefreshWindow
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBookCacheGateTest {
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var gate: UserBookCacheGate

    @BeforeEach
    fun setUp() {
        booksLocalDataSource = mockk(relaxed = true)
        gate = UserBookCacheGate(booksLocalDataSource = booksLocalDataSource)
    }

    private fun bookWithUserBook(userBookId: Int): Book = baseBook.copy(
        userBook = baseBook.userBook?.copy(id = userBookId),
    )

    private fun TestScope.openRefresh(
        fetchGate: CompletableDeferred<List<Book>>,
        windows: MutableList<RefreshWindow>,
        selectToCache: suspend (List<Book>) -> List<Book> = { it },
    ): Deferred<List<Book>> {
        val refresh: Deferred<List<Book>> = async {
            gate.refresh(
                fetch = { fetchGate.await() },
                reconcile = { fetched, window ->
                    windows += window
                    fetched
                },
                selectToCache = selectToCache,
            )
        }

        runCurrent()

        return refresh
    }

    @Nested
    inner class WritesDuringFetch {
        @Test
        fun `a write with a kind made while the fetch is suspended is handed to reconcile`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )

            // ----- Act -----
            gate.cacheBook(
                book = bookWithUserBook(userBookId = 7),
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
            )
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().writes shouldBe mapOf(7 to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS))
        }

        @Test
        fun `a write completed before the refresh started is not handed to reconcile`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            gate.cacheBook(
                book = bookWithUserBook(userBookId = 7),
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
            )

            // ----- Act -----
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().writes.shouldBeEmpty()
        }

        @Test
        fun `a write without a kind is stored but not handed to reconcile`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            val book = bookWithUserBook(userBookId = 7)
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )

            // ----- Act -----
            gate.cacheBook(book = book)
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().writes.shouldBeEmpty()
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = book)
            }
        }

        @Test
        fun `kinds written for the same user book accumulate`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            val book = bookWithUserBook(userBookId = 7)
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )

            // ----- Act -----
            gate.cacheBook(
                book = book,
                kind = PendingUserBookWriteKind.UPDATE_RATING,
            )
            gate.cacheBook(
                book = book,
                kind = PendingUserBookWriteKind.SET_STATUS,
            )
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().writes shouldBe mapOf(
                7 to setOf(
                    PendingUserBookWriteKind.UPDATE_RATING,
                    PendingUserBookWriteKind.SET_STATUS,
                ),
            )
        }

        @Test
        fun `recordWrite during the fetch is handed to reconcile without writing the row`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )

            // ----- Act -----
            gate.recordWrite(
                book = bookWithUserBook(userBookId = 7),
                kind = PendingUserBookWriteKind.MARK_AS_READ,
            )
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().writes shouldBe mapOf(7 to setOf(PendingUserBookWriteKind.MARK_AS_READ))
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `two overlapping refreshes both see a write made while both are open`() = runTest {
            // ----- Arrange -----
            val firstFetchGate = CompletableDeferred<List<Book>>()
            val secondFetchGate = CompletableDeferred<List<Book>>()
            val firstWindows = mutableListOf<RefreshWindow>()
            val secondWindows = mutableListOf<RefreshWindow>()
            val first = openRefresh(
                fetchGate = firstFetchGate,
                windows = firstWindows,
            )
            val second = openRefresh(
                fetchGate = secondFetchGate,
                windows = secondWindows,
            )

            // ----- Act -----
            gate.cacheBook(
                book = bookWithUserBook(userBookId = 7),
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
            )
            firstFetchGate.complete(emptyList())
            secondFetchGate.complete(emptyList())
            first.await()
            second.await()

            // ----- Assert -----
            val expected = mapOf(7 to setOf(PendingUserBookWriteKind.UPDATE_REVIEW))
            firstWindows.single().writes shouldBe expected
            secondWindows.single().writes shouldBe expected
        }

        @Test
        fun `a write made after one of two overlapping refreshes finished reaches only the other`() = runTest {
            // ----- Arrange -----
            val firstFetchGate = CompletableDeferred<List<Book>>()
            val secondFetchGate = CompletableDeferred<List<Book>>()
            val firstWindows = mutableListOf<RefreshWindow>()
            val secondWindows = mutableListOf<RefreshWindow>()
            val first = openRefresh(
                fetchGate = firstFetchGate,
                windows = firstWindows,
            )
            val second = openRefresh(
                fetchGate = secondFetchGate,
                windows = secondWindows,
            )
            firstFetchGate.complete(emptyList())
            first.await()

            // ----- Act -----
            gate.cacheBook(
                book = bookWithUserBook(userBookId = 7),
                kind = PendingUserBookWriteKind.UPDATE_RATING,
            )
            secondFetchGate.complete(emptyList())
            second.await()

            // ----- Assert -----
            firstWindows.single().writes.shouldBeEmpty()
            secondWindows.single().writes shouldBe mapOf(7 to setOf(PendingUserBookWriteKind.UPDATE_RATING))
        }
    }

    @Nested
    inner class RemovalsDuringFetch {
        @Test
        fun `a removal during the fetch is in the window's removed ids`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )

            // ----- Act -----
            gate.removeUserBooks(ids = listOf(3, 4))
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().removedUserBookIds shouldBe setOf(3, 4)
            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(3, 4))
            }
        }

        @Test
        fun `a later write of the same user book clears its removal`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )
            gate.removeUserBooks(ids = listOf(3, 4))

            // ----- Act -----
            gate.cacheBook(book = bookWithUserBook(userBookId = 3))
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().removedUserBookIds shouldBe setOf(4)
        }

        @Test
        fun `a removal made before the refresh started is not in the window`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            gate.removeUserBooks(ids = listOf(3))

            // ----- Act -----
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = windows,
            )
            fetchGate.complete(emptyList())
            refresh.await()

            // ----- Assert -----
            windows.single().removedUserBookIds shouldBe emptySet()
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `returns the reconciled books but caches only what selectToCache keeps`() = runTest {
            // ----- Arrange -----
            val kept = bookWithUserBook(userBookId = 1)
            val dropped = bookWithUserBook(userBookId = 2)
            val fetchGate = CompletableDeferred<List<Book>>()
            val refresh = openRefresh(
                fetchGate = fetchGate,
                windows = mutableListOf(),
                selectToCache = { books -> books.filter { it.userBook?.id == 1 } },
            )

            // ----- Act -----
            fetchGate.complete(listOf(kept, dropped))
            val result = refresh.await()

            // ----- Assert -----
            result shouldContainExactly listOf(kept, dropped)
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBooks(books = listOf(kept))
            }
        }

        @Test
        fun `a failing fetch propagates without reconciling or caching`() = runTest {
            // ----- Arrange -----
            val fetchGate = CompletableDeferred<List<Book>>()
            val windows = mutableListOf<RefreshWindow>()
            val failing: Deferred<Result<List<Book>>> = async {
                runCatching {
                    gate.refresh(
                        fetch = { fetchGate.await() },
                        reconcile = { fetched, window ->
                            windows += window
                            fetched
                        },
                        selectToCache = { it },
                    )
                }
            }
            runCurrent()
            fetchGate.completeExceptionally(IllegalStateException("offline"))

            // ----- Act -----
            val failure = failing.await().exceptionOrNull()

            // ----- Assert -----
            failure.shouldBeInstanceOf<IllegalStateException>().message shouldBe "offline"
            windows.size shouldBe 0
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBooks(books = any())
            }
        }

        @Test
        fun `a cancelled fetch releases the gate`() = runTest {
            // ----- Arrange -----
            val cancelledFetchGate = CompletableDeferred<List<Book>>()
            val cancelled = openRefresh(
                fetchGate = cancelledFetchGate,
                windows = mutableListOf(),
            )
            val written = bookWithUserBook(userBookId = 7)
            val fetched = bookWithUserBook(userBookId = 8)
            val nextFetchGate = CompletableDeferred<List<Book>>()

            // ----- Act -----
            cancelled.cancel()
            runCurrent()
            gate.cacheBook(book = written)
            val next = openRefresh(
                fetchGate = nextFetchGate,
                windows = mutableListOf(),
            )
            nextFetchGate.complete(listOf(fetched))
            val result = next.await()

            // ----- Assert -----
            result shouldContainExactly listOf(fetched)
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = written)
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBooks(books = listOf(fetched))
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBooks(books = any())
            }
        }
    }
}
