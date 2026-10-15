package nl.rhaydus.softcover.feature.settings.domain.model

import kotlinx.collections.immutable.ImmutableList

/** The parsed changelog, newest release first as published. */
data class Changelog(
    val entries: ImmutableList<ChangelogEntry>,
    val fetchedAtEpochMillis: Long?,
    val source: PublishedDocumentSource,
)
