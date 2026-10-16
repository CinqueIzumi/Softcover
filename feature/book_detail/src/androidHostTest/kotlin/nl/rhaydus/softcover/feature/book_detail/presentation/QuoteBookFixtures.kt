package nl.rhaydus.softcover.feature.book_detail.presentation

import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead

/** A shelved book with the edition and read facts the quote sheet's rules look at. */
@Suppress("LongParameterList")
internal fun buildQuoteBook(
    id: Int = 7,
    title: String = "Piranesi",
    currentPages: Int? = null,
    defaultPages: Int? = null,
    audioSeconds: Int? = null,
    isAudiobook: Boolean = false,
    readPage: Int? = null,
    readSeconds: Int? = null,
    shelved: Boolean = true,
    privacySettingId: Int = 1,
): Book {
    val currentEdition = mockk<BookEdition>().also {
        every {
            it.id
        } returns 70
        every {
            it.pages
        } returns currentPages
        every {
            it.audioSeconds
        } returns audioSeconds
        every {
            it.isAudiobook
        } returns isAudiobook
    }
    val defaultEdition = mockk<BookEdition>().also {
        every {
            it.pages
        } returns defaultPages
    }
    val read = mockk<UserBookRead>().also {
        every {
            it.currentPage
        } returns readPage
        every {
            it.currentSeconds
        } returns readSeconds
    }
    val userBook = mockk<UserBook>().also {
        every {
            it.privacySettingId
        } returns privacySettingId
    }

    return mockk<Book>().also {
        every {
            it.id
        } returns id
        every {
            it.title
        } returns title
        every {
            it.currentEdition
        } returns currentEdition
        every {
            it.defaultEdition
        } returns defaultEdition
        every {
            it.currentRead
        } returns read
        every {
            it.userBook
        } returns userBook.takeIf { shelved }
    }
}
