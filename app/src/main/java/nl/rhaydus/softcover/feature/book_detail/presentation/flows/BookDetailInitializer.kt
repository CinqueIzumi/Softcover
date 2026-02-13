package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.Initializer

sealed interface BookDetailInitializer : Initializer<
        nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState,
        nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent,
        nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables,
        >