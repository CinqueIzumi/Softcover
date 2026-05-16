package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import java.time.LocalDate

class DismissPlanTodayUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        today: LocalDate = LocalDate.now(),
    ): Result<Unit> = runCatching {
        settingsRepository.setPlanTodayDismissed(
            bookId = bookId,
            isoDate = today.toString(),
        )
    }
}
