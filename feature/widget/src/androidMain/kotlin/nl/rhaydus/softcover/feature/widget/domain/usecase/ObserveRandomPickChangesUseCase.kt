package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository

internal class ObserveRandomPickChangesUseCase(private val randomPickRepository: RandomPickRepository) {
    operator fun invoke(): Flow<Int> = randomPickRepository.changes
}
