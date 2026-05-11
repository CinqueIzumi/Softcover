package nl.rhaydus.softcover.feature.reading.presentation.util

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookProgressUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateBookProgressTest {

    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
    private lateinit var updateBookProgressUseCase: UpdateBookProgressUseCase
    private lateinit var updateBookProgress: UpdateBookProgress

    @BeforeEach
    fun setUp() {
        markBookAsReadUseCase = mockk()
        updateBookProgressUseCase = mockk()
        updateBookProgress = UpdateBookProgress(
            markBookAsReadUseCase = markBookAsReadUseCase,
            updateBookProgressUseCase = updateBookProgressUseCase,
        )
    }

    private fun stubBookWithCurrentEditionPages(pages: Int?): Book = mockk<Book>().also { book ->
        val edition = mockk<BookEdition>().also { e ->
            every { e.pages } returns pages
            every { e.audioSeconds } returns null
        }

        every { book.currentEdition } returns edition
    }

    private fun stubBookWithCurrentEditionAudioSeconds(audioSeconds: Int?): Book =
        mockk<Book>().also { book ->
            val edition = mockk<BookEdition>().also { e ->
                every { e.audioSeconds } returns audioSeconds
            }

            every { book.currentEdition } returns edition
        }

    @Nested
    inner class Invoke {

        @Test
        fun `calls setLoading with true then false`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 200)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 50)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 50,
                setLoading = { loadingStates.add(it) },
            )

            // ----- Assert -----
            loadingStates shouldBe listOf(true, false)
        }

        @Test
        fun `invokes markBookAsReadUseCase when newPage equals currentEdition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 300,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(book = book)
            }
        }

        @Test
        fun `does not invoke updateBookProgressUseCase when newPage equals currentEdition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 300,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookProgressUseCase(any(), any())
            }
        }

        @Test
        fun `invokes updateBookProgressUseCase when newPage is less than currentEdition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 150)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 150,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                updateBookProgressUseCase(book = book, newPage = 150)
            }
        }

        @Test
        fun `does not invoke markBookAsReadUseCase when newPage is less than currentEdition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 100)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 100,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                markBookAsReadUseCase(any())
            }
        }

        @Test
        fun `invokes updateBookProgressUseCase when currentEdition pages is null`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = null)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 50)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 50,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                updateBookProgressUseCase(book = book, newPage = 50)
            }
        }

        @Test
        fun `calls setLoading false even when markBookAsReadUseCase fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 300,
                setLoading = { loadingStates.add(it) },
            )

            // ----- Assert -----
            loadingStates.last() shouldBe false
        }

        @Test
        fun `calls setLoading false even when updateBookProgressUseCase fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 100)
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 100,
                setLoading = { loadingStates.add(it) },
            )

            // ----- Assert -----
            loadingStates.last() shouldBe false
        }

        @Test
        fun `does not throw when markBookAsReadUseCase fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act & Assert -----
            updateBookProgress(
                book = book,
                newPage = 300,
                setLoading = {},
            )
        }

        @Test
        fun `does not throw when updateBookProgressUseCase fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 50)
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act & Assert -----
            updateBookProgress(
                book = book,
                newPage = 50,
                setLoading = {},
            )
        }

        @Test
        fun `invokes updateBookProgressUseCase when newPage exceeds currentEdition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 350)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 350,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                updateBookProgressUseCase(book = book, newPage = 350)
            }
        }

        @Test
        fun `does not invoke markBookAsReadUseCase when newPage exceeds currentEdition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 350)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newPage = 350,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                markBookAsReadUseCase(any())
            }
        }

        @Test
        fun `invokes markBookAsReadUseCase when newSeconds equals audioSeconds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = 3600)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 3600,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(book = book)
            }
        }

        @Test
        fun `invokes markBookAsReadUseCase when newSeconds exceeds audioSeconds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = 3600)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 4000,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(book = book)
            }
        }

        @Test
        fun `does not invoke updateBookProgressUseCase when newSeconds equals audioSeconds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = 3600)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 3600,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookProgressUseCase(any(), any(), any())
            }
        }

        @Test
        fun `invokes updateBookProgressUseCase with newPage null and newSeconds when newSeconds is less than audioSeconds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = 3600)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 1800)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 1800,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 1800)
            }
        }

        @Test
        fun `does not invoke markBookAsReadUseCase when newSeconds is less than audioSeconds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = 3600)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 1800)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 1800,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                markBookAsReadUseCase(any())
            }
        }

        @Test
        fun `invokes updateBookProgressUseCase when newSeconds is non-null but audioSeconds is null`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = null)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 1800)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 1800,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 1800)
            }
        }

        @Test
        fun `does not invoke markBookAsReadUseCase when newSeconds is non-null but audioSeconds is null`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = null)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 1800)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 1800,
                setLoading = {},
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                markBookAsReadUseCase(any())
            }
        }

        @Test
        fun `calls setLoading true then false for audiobook progress update`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = 3600)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 1800)
            } returns Result.success(Unit)

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 1800,
                setLoading = { loadingStates.add(it) },
            )

            // ----- Assert -----
            loadingStates shouldBe listOf(true, false)
        }

        @Test
        fun `calls setLoading false even when marking audiobook as read fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionAudioSeconds(audioSeconds = 3600)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            updateBookProgress(
                book = book,
                newSeconds = 3600,
                setLoading = { loadingStates.add(it) },
            )

            // ----- Assert -----
            loadingStates.last() shouldBe false
        }

        @Test
        fun `returns success when updateBookProgressUseCase succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 200)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 50)
            } returns Result.success(Unit)

            // ----- Act -----
            val result = updateBookProgress(
                book = book,
                newPage = 50,
                setLoading = {},
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when updateBookProgressUseCase fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 200)
            val error = RuntimeException("network error")

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 50)
            } returns Result.failure(error)

            // ----- Act -----
            val result = updateBookProgress(
                book = book,
                newPage = 50,
                setLoading = {},
            )

            // ----- Assert -----
            result.isFailure shouldBe true
        }

        @Test
        fun `returns success when markBookAsReadUseCase succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            // ----- Act -----
            val result = updateBookProgress(
                book = book,
                newPage = 300,
                setLoading = {},
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when markBookAsReadUseCase fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)
            val error = RuntimeException("api error")

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(error)

            // ----- Act -----
            val result = updateBookProgress(
                book = book,
                newPage = 300,
                setLoading = {},
            )

            // ----- Assert -----
            result.isFailure shouldBe true
        }
    }
}
