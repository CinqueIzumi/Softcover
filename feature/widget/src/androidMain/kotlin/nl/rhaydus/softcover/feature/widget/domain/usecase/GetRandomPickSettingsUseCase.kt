package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository

internal class GetRandomPickSettingsUseCase(private val randomPickRepository: RandomPickRepository) {
    suspend operator fun invoke(appWidgetId: Int): Result<RandomPickSettings> = runCatchingCancellable {
        randomPickRepository.getSettings(appWidgetId = appWidgetId)
    }
}
