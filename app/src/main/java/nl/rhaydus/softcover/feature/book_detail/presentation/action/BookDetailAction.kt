package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction

sealed interface BookDetailAction : UiAction<
        nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState,
        nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent,
        nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables,
        >