package nl.rhaydus.softcover.feature.settings.domain.model

import kotlinx.collections.immutable.ImmutableList

/**
 * A parsed document published from the repo, ready to render. [fetchedAtEpochMillis] is `null` only for
 * [source] [PublishedDocumentSource.BUNDLED] - the build-time fallback carries no fetch timestamp of its
 * own.
 */
data class PublishedDocument(
    val blocks: ImmutableList<MarkdownBlock>,
    val fetchedAtEpochMillis: Long?,
    val source: PublishedDocumentSource,
)
