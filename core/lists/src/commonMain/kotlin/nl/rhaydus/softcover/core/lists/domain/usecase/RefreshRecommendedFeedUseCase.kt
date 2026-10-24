package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshSystemVibeBooksUseCase
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import kotlin.time.Duration

/** Refreshes every [SystemVibe] the recommended feed can fall back to; fails if any of them failed. */
class RefreshRecommendedFeedUseCase internal constructor(
    private val refreshSystemVibeBooks: RefreshSystemVibeBooksUseCase,
) {
    suspend operator fun invoke(maxAge: Duration): Result<Unit> = coroutineScope {
        SystemVibe.entries
            .map { vibe ->
                async {
                    refreshSystemVibeBooks(
                        vibe = vibe,
                        maxAge = maxAge,
                    )
                }
            }
            .awaitAll()
            .firstOrNull { result -> result.isFailure }
            ?: Result.success(Unit)
    }
}
