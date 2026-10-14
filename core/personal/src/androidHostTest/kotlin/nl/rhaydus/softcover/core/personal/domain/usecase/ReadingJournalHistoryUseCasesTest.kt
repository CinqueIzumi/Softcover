package nl.rhaydus.softcover.core.personal.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingJournalHistoryUseCasesTest {
    private lateinit var repository: ReadingJournalHistoryRepository

    @BeforeEach
    fun setUp() {
        repository = mockk()
    }

    @Nested
    inner class ObserveUseCase {
        @Test
        fun `emits the repository entries for the book`() = runTest {
            // ----- Arrange -----
            val entries = listOf(
                ReadingJournalEntry(
                    date = LocalDate(
                        2024,
                        10,
                        5,
                    ),
                    pages = 10,
                    seconds = null,
                ),
            )

            every {
                repository.observeReadingJournalHistory(bookId = 3)
            } returns flowOf(entries)

            val useCase = ObserveReadingJournalHistoryUseCase(repository = repository)

            // ----- Act -----
            val result = useCase(bookId = 3).first()

            // ----- Assert -----
            result shouldBe entries
        }
    }

    @Nested
    inner class RefreshUseCase {
        @Test
        fun `returns success after refreshing the book`() = runTest {
            // ----- Arrange -----
            coEvery {
                repository.refreshReadingJournalHistory(bookId = 3)
            } returns Unit

            val useCase = RefreshReadingJournalHistoryUseCase(repository = repository)

            // ----- Act -----
            val result = useCase(bookId = 3)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) {
                repository.refreshReadingJournalHistory(bookId = 3)
            }
        }

        @Test
        fun `surfaces a refresh failure as Result failure`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("offline")

            coEvery {
                repository.refreshReadingJournalHistory(bookId = 3)
            } throws error

            val useCase = RefreshReadingJournalHistoryUseCase(repository = repository)

            // ----- Act -----
            val result = useCase(bookId = 3)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe error
        }
    }
}
