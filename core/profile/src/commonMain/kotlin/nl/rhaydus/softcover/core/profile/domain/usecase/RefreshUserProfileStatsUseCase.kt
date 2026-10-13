package nl.rhaydus.softcover.core.profile.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

// Pages the account's entire read-book history via fetchUserProfileSnapshot()
// (GetReadUserBooksForStats, ceil(booksRead / 100) requests) to build the stats the Profile tab
// renders - booksByYear, pagesByYear, pagesByMonth, genres, ratings, trackedYears,
// authorDemographics, totalPagesRead. Nothing else needs these, so this is called only from the
// Profile tab's composed refresh, never at startup.
class RefreshUserProfileStatsUseCase(
    private val profileRepository: ProfileRepository,
    private val statsRefreshGate: ProfileRefreshGate,
) {
    // Skips the network fetch once this session has already refreshed successfully - every other
    // Profile visit renders from the DataStore cache. runOnce marks only after a successful cache
    // write, so a transient failure leaves the gate open and the next visit retries. Gated
    // independently from the activity half (RefreshReadingActivityUseCase).
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        statsRefreshGate.runOnce {
            val snapshot = profileRepository.fetchUserProfileSnapshot()

            profileRepository.cacheUserProfileStats(snapshot = snapshot)
        }
    }
}
