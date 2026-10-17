package nl.rhaydus.softcover.feature.widget.domain.util

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickSelectionTest {
    private val firstCandidate = object : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }
    private val amsterdam = TimeZone.of("Europe/Amsterdam")

    private fun book(
        id: Int,
        status: BookStatus = BookStatus.None,
    ): Book = mockk<Book> {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.status
        } returns status
    }

    private fun list(
        id: Int,
        slug: String = "list-$id",
        books: List<Book> = emptyList(),
    ): BookList = BookList(
        id = id,
        name = "List $id",
        slug = slug,
        books = books.map {
            ListBook(
                listBookId = it.id,
                listId = id,
                bookId = it.id,
                editionId = 0,
                book = it,
            )
        },
    )

    private fun at(
        day: Int,
        hour: Int,
        minute: Int = 0,
    ): Instant = LocalDateTime(
        year = 2024,
        month = 3,
        day = day,
        hour = hour,
        minute = minute,
    ).toInstant(amsterdam)

    @Nested
    inner class IsSourceAvailable {
        @Test
        fun `want to read is always available`() {
            // ----- Arrange -----
            val lists = emptyList<BookList>()

            // ----- Act -----
            val result = isSourceAvailable(
                source = RandomPickSource.WantToRead,
                lists = lists,
            )

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `owned is available only when an owned list exists`() {
            // ----- Arrange -----
            val withOwned = listOf(list(
                id = 1,
                slug = "owned",
            ),)
            val withoutOwned = listOf(list(id = 2))

            // ----- Act -----
            val present = isSourceAvailable(
                source = RandomPickSource.Owned,
                lists = withOwned,
            )
            val absent = isSourceAvailable(
                source = RandomPickSource.Owned,
                lists = withoutOwned,
            )

            // ----- Assert -----
            present shouldBe true
            absent shouldBe false
        }

        @Test
        fun `a user list is available only while it is stored`() {
            // ----- Arrange -----
            val lists = listOf(list(1))

            // ----- Act -----
            val stored = isSourceAvailable(
                source = RandomPickSource.UserList(listId = 1),
                lists = lists,
            )
            val missing = isSourceAvailable(
                source = RandomPickSource.UserList(listId = 99),
                lists = lists,
            )

            // ----- Assert -----
            stored shouldBe true
            missing shouldBe false
        }
    }

    @Nested
    inner class BuildPool {
        @Test
        fun `want to read returns the shelf`() {
            // ----- Arrange -----
            val shelf = (1..2).map { book(it) }

            // ----- Act -----
            val result = buildPool(
                source = RandomPickSource.WantToRead,
                wantToRead = shelf,
                lists = emptyList(),
            )

            // ----- Assert -----
            result.map { it.id } shouldContainExactly listOf(1, 2)
        }

        @Test
        fun `owned uses the owned list`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 1,
                    books = listOf(book(10)),
                ),
                list(
                    id = 2,
                    slug = "owned",
                    books = listOf(book(20)),
                ),
            )

            // ----- Act -----
            val result = buildPool(
                source = RandomPickSource.Owned,
                wantToRead = listOf(book(99)),
                lists = lists,
            )

            // ----- Assert -----
            result.map { it.id } shouldContainExactly listOf(20)
        }

        @Test
        fun `a user list source uses that list`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 1,
                    books = listOf(book(10)),
                ),
                list(
                    id = 2,
                    books = listOf(book(20)),
                ),
            )

            // ----- Act -----
            val result = buildPool(
                source = RandomPickSource.UserList(listId = 2),
                wantToRead = listOf(book(99)),
                lists = lists,
            )

            // ----- Assert -----
            result.map { it.id } shouldContainExactly listOf(20)
        }

        @Test
        fun `owned and list pools keep only want to read and unshelved books`() {
            // ----- Arrange -----
            val books = listOf(
                book(
                    id = 1,
                    status = BookStatus.None,
                ),
                book(
                    id = 2,
                    status = BookStatus.WantToRead,
                ),
                book(
                    id = 3,
                    status = BookStatus.Paused,
                ),
                book(
                    id = 4,
                    status = BookStatus.DidNotFinish,
                ),
                book(
                    id = 5,
                    status = BookStatus.Read,
                ),
                book(
                    id = 6,
                    status = BookStatus.Reading,
                ),
            )
            val lists = listOf(
                list(
                    id = 1,
                    slug = "owned",
                    books = books,
                ),
                list(
                    id = 2,
                    books = books,
                ),
            )

            // ----- Act -----
            val owned = buildPool(
                source = RandomPickSource.Owned,
                wantToRead = emptyList(),
                lists = lists,
            )
            val userList = buildPool(
                source = RandomPickSource.UserList(listId = 2),
                wantToRead = emptyList(),
                lists = lists,
            )

            // ----- Assert -----
            owned.map { it.id } shouldContainExactly listOf(1, 2)
            userList.map { it.id } shouldContainExactly listOf(1, 2)
        }

        @Test
        fun `books are de-duplicated by id`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 1,
                    books = listOf(1, 1, 2).map { book(it) },
                ),
            )

            // ----- Act -----
            val result = buildPool(
                source = RandomPickSource.UserList(listId = 1),
                wantToRead = emptyList(),
                lists = lists,
            )

            // ----- Assert -----
            result.map { it.id } shouldContainExactly listOf(1, 2)
        }

        @Test
        fun `a missing list gives an empty pool instead of the want to read shelf`() {
            // ----- Arrange -----
            val shelf = listOf(book(7))

            // ----- Act -----
            val result = buildPool(
                source = RandomPickSource.UserList(listId = 99),
                wantToRead = shelf,
                lists = emptyList(),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `owned without an owned list gives an empty pool`() {
            // ----- Arrange -----
            val lists = listOf(list(
                id = 1,
                books = listOf(book(10)),
            ),)

            // ----- Act -----
            val result = buildPool(
                source = RandomPickSource.Owned,
                wantToRead = listOf(book(99)),
                lists = lists,
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class ResolvePick {
        private val pool = (1..2).map { book(it) }

        private fun resolve(
            state: RandomPickState,
            cadence: RandomPickCadence,
            now: Instant,
            pool: List<Book> = this.pool,
        ): RandomPickState = resolvePick(
            state = state,
            cadence = cadence,
            pool = pool,
            now = now,
            timeZone = amsterdam,
            random = firstCandidate,
        )

        @Test
        fun `daily keeps the pick until local midnight`() {
            // ----- Arrange -----
            val state = RandomPickState(
                pickedBookId = 2,
                pickedAtMillis = at(
                    day = 10,
                    hour = 0,
                ).toEpochMilliseconds(),
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.Daily,
                now = at(
                    day = 10,
                    hour = 23,
                    minute = 59,
                ),
            )

            // ----- Assert -----
            result shouldBe state
        }

        @Test
        fun `daily re-picks after local midnight`() {
            // ----- Arrange -----
            val state = RandomPickState(
                pickedBookId = 2,
                pickedAtMillis = at(
                    day = 10,
                    hour = 23,
                    minute = 59,
                ).toEpochMilliseconds(),
            )
            val now = at(
                day = 11,
                hour = 0,
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.Daily,
                now = now,
            )

            // ----- Assert -----
            result shouldBe RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = now.toEpochMilliseconds(),
            )
        }

        @Test
        fun `weekly keeps the pick just before seven days`() {
            // ----- Arrange -----
            val pickedAt = at(
                day = 1,
                hour = 9,
            )
            val state = RandomPickState(
                pickedBookId = 2,
                pickedAtMillis = pickedAt.toEpochMilliseconds(),
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.Weekly,
                now = pickedAt + 7.days - 1.milliseconds,
            )

            // ----- Assert -----
            result shouldBe state
        }

        @Test
        fun `weekly re-picks at seven days`() {
            // ----- Arrange -----
            val pickedAt = at(
                day = 1,
                hour = 9,
            )
            val now = pickedAt + 7.days
            val state = RandomPickState(
                pickedBookId = 2,
                pickedAtMillis = pickedAt.toEpochMilliseconds(),
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.Weekly,
                now = now,
            )

            // ----- Assert -----
            result shouldBe RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = now.toEpochMilliseconds(),
            )
        }

        @Test
        fun `on shuffle only never expires`() {
            // ----- Arrange -----
            val pickedAt = at(
                day = 1,
                hour = 9,
            )
            val state = RandomPickState(
                pickedBookId = 2,
                pickedAtMillis = pickedAt.toEpochMilliseconds(),
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.OnShuffleOnly,
                now = pickedAt + 3650.days,
            )

            // ----- Assert -----
            result shouldBe state
        }

        @Test
        fun `re-picks when the picked book left the pool`() {
            // ----- Arrange -----
            val now = at(
                day = 10,
                hour = 10,
            )
            val state = RandomPickState(
                pickedBookId = 99,
                pickedAtMillis = at(
                    day = 10,
                    hour = 8,
                ).toEpochMilliseconds(),
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.Daily,
                now = now,
            )

            // ----- Assert -----
            result shouldBe RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = now.toEpochMilliseconds(),
            )
        }

        @Test
        fun `picks when nothing was picked yet`() {
            // ----- Arrange -----
            val now = at(
                day = 10,
                hour = 10,
            )

            // ----- Act -----
            val result = resolve(
                state = RandomPickState(),
                cadence = RandomPickCadence.OnShuffleOnly,
                now = now,
            )

            // ----- Assert -----
            result shouldBe RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = now.toEpochMilliseconds(),
            )
        }

        @Test
        fun `a re-pick excludes the current book when the pool has more than one`() {
            // ----- Arrange -----
            val state = RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = at(
                    day = 1,
                    hour = 8,
                ).toEpochMilliseconds(),
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.Daily,
                now = at(
                    day = 10,
                    hour = 10,
                ),
            )

            // ----- Assert -----
            result.pickedBookId shouldBe 2
        }

        @Test
        fun `an empty pool gives no pick`() {
            // ----- Arrange -----
            val state = RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = at(
                    day = 10,
                    hour = 8,
                ).toEpochMilliseconds(),
            )

            // ----- Act -----
            val result = resolve(
                state = state,
                cadence = RandomPickCadence.Daily,
                now = at(
                    day = 10,
                    hour = 10,
                ),
                pool = emptyList(),
            )

            // ----- Assert -----
            result shouldBe RandomPickState()
        }
    }

    @Nested
    inner class ShufflePick {
        @Test
        fun `always changes the pick when the pool has more than one book`() {
            // ----- Arrange -----
            val now = at(
                day = 10,
                hour = 10,
            )
            val state = RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = 0L,
            )

            // ----- Act -----
            val result = shufflePick(
                state = state,
                pool = (1..3).map { book(it) },
                now = now,
                random = firstCandidate,
            )

            // ----- Assert -----
            result shouldBe RandomPickState(
                pickedBookId = 2,
                pickedAtMillis = now.toEpochMilliseconds(),
            )
        }

        @Test
        fun `a single book pool returns that book again`() {
            // ----- Arrange -----
            val now = at(
                day = 10,
                hour = 10,
            )
            val state = RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = 0L,
            )

            // ----- Act -----
            val result = shufflePick(
                state = state,
                pool = listOf(book(1)),
                now = now,
                random = firstCandidate,
            )

            // ----- Assert -----
            result shouldBe RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = now.toEpochMilliseconds(),
            )
        }

        @Test
        fun `an empty pool gives no pick`() {
            // ----- Arrange -----
            val state = RandomPickState(
                pickedBookId = 1,
                pickedAtMillis = 0L,
            )

            // ----- Act -----
            val result = shufflePick(
                state = state,
                pool = emptyList(),
                now = at(
                    day = 10,
                    hour = 10,
                ),
                random = firstCandidate,
            )

            // ----- Assert -----
            result.pickedBookId.shouldBeNull()
            result.pickedAtMillis.shouldBeNull()
        }
    }
}
