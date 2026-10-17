package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.repository.CurrentlyReadingRepository

internal class CycleSelectedBookUseCase(private val currentlyReadingRepository: CurrentlyReadingRepository) {
    suspend operator fun invoke(
        appWidgetId: Int,
        orderedBookIds: List<Int>,
        step: Int,
    ): Result<Unit> = runCatchingCancellable {
        currentlyReadingRepository.cycleSelectedBook(
            appWidgetId = appWidgetId,
            orderedBookIds = orderedBookIds,
            step = step,
        )
    }
}
