package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettingsChange
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository

internal class ObserveRandomPickSettingsChangesUseCase(private val randomPickRepository: RandomPickRepository) {
    operator fun invoke(): Flow<RandomPickSettingsChange> = randomPickRepository.settingsChanges
}
