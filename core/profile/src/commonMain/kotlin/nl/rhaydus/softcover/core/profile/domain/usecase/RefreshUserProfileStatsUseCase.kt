package nl.rhaydus.softcover.core.profile.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.usecase.HasSyncedReadBooksThisSessionUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

// Refreshes what the Profile tab and the Stats Atlas are built from: the account's identity (one
// request, cached in the profile DataStore) and the Read books the aggregates are computed from,
// which live in Room. Called from the Profile tab and after re-auth, never at startup.
class RefreshUserProfileStatsUseCase internal constructor(
    private val profileRepository: ProfileRepository,
    private val statsRefreshGate: ProfileRefreshGate,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val hasSyncedReadBooksThisSessionUseCase: HasSyncedReadBooksThisSessionUseCase,
) {
    // Skips the whole refresh once this session has already completed it - every other Profile visit
    // renders from the DataStore cache and Room. runOnce marks only after both halves succeed, so a
    // transient failure leaves the gate open and the next visit retries.
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        statsRefreshGate.runOnce {
            val snapshot = profileRepository.fetchUserProfileSnapshot()

            profileRepository.cacheUserProfileIdentity(snapshot = snapshot)

            // A refresh that already covered Read this session (re-auth, onboarding, the start-up
            // backfill) left the same rows in Room, so fetching them again would only repeat it.
            if (hasSyncedReadBooksThisSessionUseCase().not()) {
                refreshLibraryUseCase(scope = RefreshScope.ByStatuses(setOf(UserBookStatus.READ))).getOrThrow()
            }
        }
    }
}
