package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookCopiesUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookTraitsUseCase
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.core.profile.domain.mapper.dnfBook
import nl.rhaydus.softcover.core.profile.domain.mapper.readBook
import nl.rhaydus.softcover.core.profile.domain.model.CharacterBreakdown
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveDiversityStatsUseCaseTest {
    private lateinit var getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var getPausedUserBooksUseCase: GetPausedUserBooksUseCase
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase
    private lateinit var observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase
    private lateinit var observeAllBookTraitsUseCase: ObserveAllBookTraitsUseCase
    private lateinit var observeAllBookCopiesUseCase: ObserveAllBookCopiesUseCase
    private lateinit var dispatcher: TestDispatcher
    private lateinit var useCase: ObserveDiversityStatsUseCase

    @BeforeEach
    fun setUp() {
        getCurrentlyReadingUserBooksUseCase = mockk()
        getPausedUserBooksUseCase = mockk()
        getReadUserBooksUseCase = mockk()
        getDidNotFinishUserBooksUseCase = mockk()
        observeProgressUpdatesUseCase = mockk()
        observeAllBookTraitsUseCase = mockk()
        observeAllBookCopiesUseCase = mockk()

        every {
            getCurrentlyReadingUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            getPausedUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            getReadUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            getDidNotFinishUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            observeProgressUpdatesUseCase()
        } returns flowOf(emptyList<ProgressUpdate>())

        every {
            observeAllBookTraitsUseCase()
        } returns flowOf(emptyMap())

        every {
            observeAllBookCopiesUseCase()
        } returns flowOf(emptyMap())

        dispatcher = StandardTestDispatcher()

        useCase = ObserveDiversityStatsUseCase(
            getCurrentlyReadingUserBooksUseCase = getCurrentlyReadingUserBooksUseCase,
            getPausedUserBooksUseCase = getPausedUserBooksUseCase,
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            getDidNotFinishUserBooksUseCase = getDidNotFinishUserBooksUseCase,
            observeProgressUpdatesUseCase = observeProgressUpdatesUseCase,
            observeAllBookTraitsUseCase = observeAllBookTraitsUseCase,
            observeAllBookCopiesUseCase = observeAllBookCopiesUseCase,
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
            timeZone = TimeZone.UTC,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `merges the reading, paused, read and did not finish books`() = runTest(dispatcher) {
            // ----- Arrange -----
            every {
                getCurrentlyReadingUserBooksUseCase()
            } returns flowOf(listOf(readBook(id = 1)))

            every {
                getPausedUserBooksUseCase()
            } returns flowOf(listOf(readBook(id = 2)))

            every {
                getReadUserBooksUseCase()
            } returns flowOf(listOf(readBook(id = 3)))

            every {
                getDidNotFinishUserBooksUseCase()
            } returns flowOf(listOf(dnfBook(id = 4)))

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                awaitItem().booksRead shouldBe 4
                awaitComplete()
            }
        }

        @Test
        fun `re-emits with the new character counts when the traits change`() = runTest(dispatcher) {
            // ----- Arrange -----
            every {
                getReadUserBooksUseCase()
            } returns flowOf(
                listOf(
                    readBook(
                        id = 1,
                        finishedAt = "2026-01-01",
                    ),
                ),
            )

            val traitsFlow = MutableSharedFlow<Map<Int, BookTraits>>(replay = 1)
            traitsFlow.tryEmit(emptyMap())

            every {
                observeAllBookTraitsUseCase()
            } returns traitsFlow

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                awaitItem().characterRepresentation.bipoc shouldBe CharacterBreakdown(notAnsweredCount = 1)

                traitsFlow.emit(
                    mapOf(
                        1 to BookTraits(
                            bookId = 1,
                            characters = mapOf(CharacterTrait.BIPOC to TraitAnswer.YES),
                        ),
                    ),
                )

                awaitItem().characterRepresentation.bipoc shouldBe CharacterBreakdown(yesCount = 1)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `re-emits with the new acquisition counts when the copies change`() = runTest(dispatcher) {
            // ----- Arrange -----
            every {
                getReadUserBooksUseCase()
            } returns flowOf(
                listOf(
                    readBook(
                        id = 1,
                        finishedAt = "2026-01-01",
                    ),
                ),
            )

            val copiesFlow = MutableSharedFlow<Map<Int, BookCopy>>(replay = 1)
            copiesFlow.tryEmit(emptyMap())

            every {
                observeAllBookCopiesUseCase()
            } returns copiesFlow

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                awaitItem().publication.acquisition.notNoted shouldBe 1

                copiesFlow.emit(
                    mapOf(
                        1 to BookCopy(
                            bookId = 1,
                            source = AcquisitionSource.Preset(AcquisitionKind.LIBRARY),
                        ),
                    ),
                )

                val acquisition = awaitItem().publication.acquisition
                acquisition.byKind shouldBe mapOf(AcquisitionKind.LIBRARY to 1)
                acquisition.notNoted shouldBe 0
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
