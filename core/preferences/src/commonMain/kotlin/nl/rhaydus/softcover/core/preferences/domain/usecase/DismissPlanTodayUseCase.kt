package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class DismissPlanTodayUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): Result<Unit> = runCatchingLogged {
        settingsRepository.setPlanTodayDismissed(
            bookId = bookId,
            isoDate = today.toString(),
        )
    }
}
