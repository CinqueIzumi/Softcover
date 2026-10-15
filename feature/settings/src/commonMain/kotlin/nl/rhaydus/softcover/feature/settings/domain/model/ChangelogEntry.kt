package nl.rhaydus.softcover.feature.settings.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

/** One released [version] of the app and what changed in it; [blocks] excludes the heading and the release date line. */
data class ChangelogEntry(
    val version: String,
    val headline: String,
    val releaseDate: LocalDate?,
    val blocks: ImmutableList<MarkdownBlock>,
)
