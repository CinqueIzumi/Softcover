package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

sealed interface BookDetailInitializer : Initializer<
        BookDetailUiState,
        BookDetailEvent,
        BookDetailDependencies,
        BookDetailLocalVariables,
        >