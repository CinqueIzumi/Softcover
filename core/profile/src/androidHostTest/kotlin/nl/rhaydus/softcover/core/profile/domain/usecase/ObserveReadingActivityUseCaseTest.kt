package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.mapper.readBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ObserveReadingActivityUseCaseTest {
    private lateinit var observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase

    @BeforeEach
    fun setUp() {
        observeProgressUpdatesUseCase = mockk()
        getReadUserBooksUseCase = mockk()
        every {
            getReadUserBooksUseCase()
        } returns flowOf(emptyList<Book>())
    }

    private fun TestScope.buildUseCase(): ObserveReadingActivityUseCase {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        return ObserveReadingActivityUseCase(
            observeProgressUpdatesUseCase = observeProgressUpdatesUseCase,
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            timeZone = TimeZone.UTC,
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
        )
    }

    private fun update(
        at: String,
        pagesRead: Int,
        pages: Int? = null,
    ) = ProgressUpdate(
        bookId = 1,
        at = Instant.parse(at),
        pagesRead = pagesRead,
        pages = pages,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `re-emits when a new progress update arrives`() = runTest {
            // ----- Arrange -----
            val updates = MutableStateFlow(
                listOf(
                    update(
                        at = "2026-03-10T10:00:00Z",
                        pagesRead = 20,
                    ),
                ),
            )
            every {
                observeProgressUpdatesUseCase()
            } returns updates

            // ----- Act & Assert -----
            buildUseCase()().test {
                awaitItem().pagesByDay.values.sum() shouldBe 20

                updates.value = updates.value + update(
                    at = "2026-03-11T10:00:00Z",
                    pagesRead = 30,
                )

                val next = awaitItem()
                next.pagesByDay.keys.size shouldBe 2
                next.streaks.size shouldBe 1
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `re-emits when a finished book's remainder appears`() = runTest {
            // ----- Arrange -----
            val readBooks = MutableStateFlow(emptyList<Book>())
            every {
                observeProgressUpdatesUseCase()
            } returns flowOf(
                listOf(
                    update(
                        at = "2026-03-10T10:00:00Z",
                        pagesRead = 40,
                        pages = 40,
                    ),
                ),
            )
            every {
                getReadUserBooksUseCase()
            } returns readBooks

            // ----- Act & Assert -----
            buildUseCase()().test {
                awaitItem().pagesByDay.values.sum() shouldBe 40

                readBooks.value = listOf(
                    readBook(
                        id = 1,
                        finishedAt = "2026-03-12",
                        pages = 100,
                    ),
                )

                awaitItem().pagesByDay[LocalDate(
                    2026,
                    3,
                    12,
                )] shouldBe 60
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `skips an emission that yields an equal activity`() = runTest {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 20,
                ),
            )
            every {
                observeProgressUpdatesUseCase()
            } returns flowOf(
                updates,
                updates,
            )

            // ----- Act & Assert -----
            buildUseCase()().test {
                awaitItem().pagesByDay.keys shouldBe setOf(
                    LocalDate(
                        year = 2026,
                        month = 3,
                        day = 10,
                    ),
                )
                awaitComplete()
            }
        }
    }
}
