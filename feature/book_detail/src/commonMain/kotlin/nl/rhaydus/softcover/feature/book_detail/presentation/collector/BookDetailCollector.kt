package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.Collector

internal sealed interface BookDetailCollector : Collector<
        BookDetailUiState,
        BookDetailEvent,
        BookDetailDependencies,
        BookDetailLocalVariables,
        >
