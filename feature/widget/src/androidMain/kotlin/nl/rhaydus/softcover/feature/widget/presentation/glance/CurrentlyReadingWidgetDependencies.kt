package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.CycleSelectedBookUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetSelectedBookIdUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveSelectedBookChangesUseCase

internal class CurrentlyReadingWidgetDependencies(
    val environment: WidgetEnvironment,
    val repainter: WidgetRepainter,
    val getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    val getSelectedBookId: GetSelectedBookIdUseCase,
    val cycleSelectedBook: CycleSelectedBookUseCase,
    val observeSelectedBookChanges: ObserveSelectedBookChangesUseCase,
)
