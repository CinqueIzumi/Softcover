package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.FillEditionLanguagesUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FillReaderEditionLanguagesUseCaseTest {
    private lateinit var getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var getPausedUserBooksUseCase: GetPausedUserBooksUseCase
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase
    private lateinit var fillEditionLanguagesUseCase: FillEditionLanguagesUseCase
    private lateinit var useCase: FillReaderEditionLanguagesUseCase

    @BeforeEach
    fun setUp() {
        getCurrentlyReadingUserBooksUseCase = mockk()
        getPausedUserBooksUseCase = mockk()
        getReadUserBooksUseCase = mockk()
        getDidNotFinishUserBooksUseCase = mockk()
        fillEditionLanguagesUseCase = mockk()

        arrange()

        coEvery {
            fillEditionLanguagesUseCase(editionIds = any())
        } returns Result.success(Unit)

        useCase = FillReaderEditionLanguagesUseCase(
            getCurrentlyReadingUserBooksUseCase = getCurrentlyReadingUserBooksUseCase,
            getPausedUserBooksUseCase = getPausedUserBooksUseCase,
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            getDidNotFinishUserBooksUseCase = getDidNotFinishUserBooksUseCase,
            fillEditionLanguagesUseCase = fillEditionLanguagesUseCase,
        )
    }

    private fun arrange(
        reading: Flow<List<Book>> = flowOf(emptyList()),
        paused: Flow<List<Book>> = flowOf(emptyList()),
        read: Flow<List<Book>> = flowOf(emptyList()),
        didNotFinish: Flow<List<Book>> = flowOf(emptyList()),
    ) {
        every {
            getCurrentlyReadingUserBooksUseCase()
        } returns reading

        every {
            getPausedUserBooksUseCase()
        } returns paused

        every {
            getReadUserBooksUseCase()
        } returns read

        every {
            getDidNotFinishUserBooksUseCase()
        } returns didNotFinish
    }

    private fun book(
        editionId: Int?,
        languageFetched: Boolean = false,
    ): Book {
        val edition = editionId?.let {
            mockk<BookEdition> {
                every {
                    id
                } returns it

                every {
                    isLanguageFetched
                } returns languageFetched
            }
        }

        return mockk {
            every {
                readerEdition
            } returns edition
        }
    }

    @Nested
    inner class Invoke {
        @Test
        fun `fills the unfetched reader editions across all four statuses`() = runTest {
            // ----- Arrange -----
            arrange(
                reading = flowOf(listOf(book(editionId = 1))),
                paused = flowOf(listOf(book(editionId = 2))),
                read = flowOf(listOf(book(editionId = 3))),
                didNotFinish = flowOf(listOf(book(editionId = 4))),
            )

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { fillEditionLanguagesUseCase(editionIds = setOf(1, 2, 3, 4)) }
        }

        @Test
        fun `leaves out books without a reader edition and editions whose language is fetched`() = runTest {
            // ----- Arrange -----
            arrange(
                reading = flowOf(listOf(book(editionId = null), book(editionId = 1))),
                read = flowOf(
                    listOf(
                        book(
                            editionId = 2,
                            languageFetched = true,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) { fillEditionLanguagesUseCase(editionIds = setOf(1)) }
        }

        @Test
        fun `passes a shared edition id once`() = runTest {
            // ----- Arrange -----
            arrange(
                reading = flowOf(listOf(book(editionId = 7))),
                paused = flowOf(listOf(book(editionId = 7))),
            )

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) { fillEditionLanguagesUseCase(editionIds = setOf(7)) }
        }

        @Test
        fun `does not fill when every edition is already fetched or missing`() = runTest {
            // ----- Arrange -----
            arrange(
                reading = flowOf(
                    listOf(
                        book(
                            editionId = 1,
                            languageFetched = true,
                        ),
                        book(editionId = null),
                    ),
                ),
            )

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { fillEditionLanguagesUseCase(editionIds = any()) }
        }

        @Test
        fun `does not fill when a source emits nothing`() = runTest {
            // ----- Arrange -----
            arrange(read = flow { })

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { fillEditionLanguagesUseCase(editionIds = any()) }
        }

        @Test
        fun `returns the failure when a source flow throws`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("read failed")
            arrange(paused = flow { throw exception })

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.exceptionOrNull() shouldBe exception
            coVerify(exactly = 0) { fillEditionLanguagesUseCase(editionIds = any()) }
        }
    }
}
