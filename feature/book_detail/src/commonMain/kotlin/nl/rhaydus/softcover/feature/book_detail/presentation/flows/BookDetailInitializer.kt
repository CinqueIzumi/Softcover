package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.Collector

internal sealed interface BookDetailInitializer : Collector<
        BookDetailUiState,
        BookDetailEvent,
        BookDetailDependencies,
        BookDetailLocalVariables,
        >
