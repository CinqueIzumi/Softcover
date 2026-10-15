package nl.rhaydus.softcover.orchestration.usecase

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

/**
 * Start-up library refresh. Covers only Currently Reading and Paused, because a full refetch of a
 * large library exhausts the API's per-minute budget. Until one all-status refresh has succeeded it
 * covers every status instead, so books cached before every read was synced pick up their full read history.
 */
internal class RefreshLibraryOnStartUseCase(
    private val settingsRepository: SettingsRepository,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        // Defaults to true on an empty read so an unreadable preference never triggers a full sync.
        val backfilled: Boolean = settingsRepository.readThroughsBackfilled.firstOrNull() ?: true

        if (backfilled) {
            refreshLibraryUseCase(scope = ACTIVE_SCOPE).getOrThrow()
            return@runCatchingLogged
        }

        refreshLibraryUseCase(scope = RefreshScope.ByStatuses(statuses = UserBookStatus.entries.toSet()))
            .getOrThrow()

        settingsRepository.markReadThroughsBackfilled()
    }

    private companion object {
        val ACTIVE_SCOPE = RefreshScope.ByStatuses(
            statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED),
        )
    }
}
