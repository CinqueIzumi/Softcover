package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.repository.CurrentlyReadingRepository

internal class GetSelectedBookIdUseCase(private val currentlyReadingRepository: CurrentlyReadingRepository) {
    suspend operator fun invoke(appWidgetId: Int): Result<Int?> = runCatchingCancellable {
        currentlyReadingRepository.getSelectedBookId(appWidgetId = appWidgetId)
    }
}
