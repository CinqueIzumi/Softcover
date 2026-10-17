package nl.rhaydus.softcover.core.component.state

import nl.rhaydus.softcover.core.component.callout.BannerUiModel

/**
 * The offline notices, in the library's own words: every surface that reports being offline reports
 * it identically, so the library owns the copy.
 */
interface OfflineCopy {
    suspend fun emptyState(): EmptyStateUiModel

    suspend fun banner(): BannerUiModel
}
