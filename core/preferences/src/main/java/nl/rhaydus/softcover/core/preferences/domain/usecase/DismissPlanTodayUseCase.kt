package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
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
