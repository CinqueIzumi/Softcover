package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.CycleSelectedBookUseCase

internal class CurrentlyReadingWidgetDependencies(
    val environment: WidgetEnvironment,
    val repainter: WidgetRepainter,
    val getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    val cycleSelectedBook: CycleSelectedBookUseCase,
)
