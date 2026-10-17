package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.repository.CurrentlyReadingRepository

internal class ObserveSelectedBookChangesUseCase(private val currentlyReadingRepository: CurrentlyReadingRepository) {
    operator fun invoke(): Flow<Int> = currentlyReadingRepository.selectionChanges
}
