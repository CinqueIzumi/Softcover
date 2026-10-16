package nl.rhaydus.softcover.feature.widget.presentation.action

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies

internal fun stubDependencies(testScope: TestScope): UpdateProgressDependencies {
    val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

    return mockk<UpdateProgressDependencies>(relaxed = true).also { mock ->
        every {
            mock.coroutineScope
        } returns testScope

        every {
            mock.mainDispatcher
        } returns dispatcher

        every {
            mock.launch(any())
        } answers { callOriginal() }
    }
}

internal fun stubPagedBook(
    pages: Int?,
    id: Int = 99,
): Book =
    mockk<Book>().also { book ->
        val edition = mockk<BookEdition>().also { e ->
            every {
                e.pages
            } returns pages
            every {
                e.isAudiobook
            } returns false
            every {
                e.audioSeconds
            } returns null
        }

        every {
            book.id
        } returns id
        every {
            book.currentEdition
        } returns edition
        every {
            book.defaultEdition
        } returns null
    }

internal fun stubAudiobook(
    audioSeconds: Int?,
    id: Int = 99,
): Book =
    mockk<Book>().also { book ->
        val edition = mockk<BookEdition>().also { e ->
            every {
                e.isAudiobook
            } returns true
            every {
                e.audioSeconds
            } returns audioSeconds
        }

        every {
            book.id
        } returns id
        every {
            book.currentEdition
        } returns edition
        every {
            book.defaultEdition
        } returns null
    }
