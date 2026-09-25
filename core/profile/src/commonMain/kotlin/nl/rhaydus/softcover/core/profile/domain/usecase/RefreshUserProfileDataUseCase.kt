package nl.rhaydus.softcover.core.profile.domain.usecase

import nl.rhaydus.common.runCatchingLogged

// Composes both refresh halves for callers that need the whole profile at once: the Profile tab
// (which renders the stats sections) and post-re-auth. Startup and the Reading screen call
// RefreshReadingActivityUseCase directly instead, so they never pay for the stats pages this pulls
// in. The activity half shares its gate with those callers, so by the time this runs from the
// Profile tab it has usually already been satisfied by startup and no-ops for free.
class RefreshUserProfileDataUseCase(
    private val refreshReadingActivityUseCase: RefreshReadingActivityUseCase,
    private val refreshUserProfileStatsUseCase: RefreshUserProfileStatsUseCase,
) {
    /**
     * Attempts **both** halves regardless of the other's outcome, then reports the first failure.
     *
     * Short-circuiting on the activity half would undo the split: the two have independent gates
     * precisely so one can succeed while the other is deferred or failing. Chaining them meant that a
     * transient failure fetching the streak also skipped the stats fetch, leaving the Profile tab's
     * charts empty for a reason that had nothing to do with them — and, because the stats gate stays
     * open on a skip, not even marked as needing a retry.
     */
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        val activity: Result<Unit> = refreshReadingActivityUseCase()
        val stats: Result<Unit> = refreshUserProfileStatsUseCase()

        activity.getOrThrow()
        stats.getOrThrow()
    }
}
