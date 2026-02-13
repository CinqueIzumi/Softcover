package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.screenmodel.BookDetailDependencies

sealed interface BookDetailAction : UiAction<
        BookDetailDependencies,
        BookDetailUiState,
        BookDetailEvent,
        BookDetailLocalVariables,
        >