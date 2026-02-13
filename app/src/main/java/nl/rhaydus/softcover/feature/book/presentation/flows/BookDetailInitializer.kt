package nl.rhaydus.softcover.feature.book.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.screenmodel.BookDetailDependencies

sealed interface BookDetailInitializer : Initializer<
        BookDetailUiState,
        BookDetailEvent,
        BookDetailDependencies,
        BookDetailLocalVariables,
        >