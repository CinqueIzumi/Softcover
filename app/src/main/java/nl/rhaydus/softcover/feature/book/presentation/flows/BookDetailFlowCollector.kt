package nl.rhaydus.softcover.feature.book.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.FlowCollector
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailDependencies

sealed interface BookDetailFlowCollector :
    FlowCollector<BookDetailUiState, BookDetailEvent, BookDetailDependencies>