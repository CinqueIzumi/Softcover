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
 * The same applies until every read's finish-date precision and edition pages have been stored.
 * Until author demographics, page counts and contributor roles have been refilled it covers the Read
 * status, the only one the reading stats need.
 */
internal class RefreshLibraryOnStartUseCase(
    private val settingsRepository: SettingsRepository,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        // Each flag defaults to true on an empty read so an unreadable preference never triggers a wide sync.
        val readThroughsBackfilled: Boolean = settingsRepository.readThroughsBackfilled.firstOrNull() ?: true
        val demographicsBackfilled: Boolean = settingsRepository.authorDemographicsBackfilled.firstOrNull() ?: true
        val rolesBackfilled: Boolean = settingsRepository.authorRolesBackfilled.firstOrNull() ?: true
        val readDatesBackfilled: Boolean = settingsRepository.readDatesBackfilled.firstOrNull() ?: true

        if (readThroughsBackfilled.not() || readDatesBackfilled.not()) {
            refreshLibraryUseCase(scope = RefreshScope.ByStatuses(statuses = UserBookStatus.entries.toSet()))
                .getOrThrow()

            settingsRepository.markReadThroughsBackfilled()
            return@runCatchingLogged
        }

        if (demographicsBackfilled.not() || rolesBackfilled.not()) {
            refreshLibraryUseCase(scope = READ_SCOPE).getOrThrow()
            return@runCatchingLogged
        }

        refreshLibraryUseCase(scope = ACTIVE_SCOPE).getOrThrow()
    }

    private companion object {
        val READ_SCOPE = RefreshScope.ByStatuses(statuses = setOf(UserBookStatus.READ))

        val ACTIVE_SCOPE = RefreshScope.ByStatuses(
            statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED),
        )
    }
}
