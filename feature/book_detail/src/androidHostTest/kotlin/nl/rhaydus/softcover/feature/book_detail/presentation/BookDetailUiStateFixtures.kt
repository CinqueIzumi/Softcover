package nl.rhaydus.softcover.feature.book_detail.presentation

import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

internal const val TEST_BOOK_ID = 1

internal fun buildBookDetailUiState(bookId: Int = TEST_BOOK_ID): BookDetailUiState = BookDetailUiState(bookId = bookId)
