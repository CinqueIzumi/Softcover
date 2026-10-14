package nl.rhaydus.softcover.feature.settings.data.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.settings.domain.model.Changelog
import nl.rhaydus.softcover.feature.settings.domain.model.ChangelogEntry
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument

private const val ENTRY_HEADING_LEVEL = 2
private const val VERSION_SEPARATOR = ": "

private class EntryHeading(
    val version: String,
    val headline: String,
)

/**
 * Groups the document into releases, each opened by a level-2 `<version>: <headline>` heading. Content
 * before the first release is dropped, and so is a level-2 section whose heading does not parse as a
 * release heading. Never throws - an unreadable document yields no entries.
 */
internal fun PublishedDocument.toChangelog(): Changelog {
    val entries = mutableListOf<ChangelogEntry>()
    var heading: EntryHeading? = null
    var body = mutableListOf<MarkdownBlock>()

    fun closeEntry() {
        heading?.let { entries += it.toEntry(body = body) }
    }

    for (block in blocks) {
        if (block is MarkdownBlock.Heading && block.level == ENTRY_HEADING_LEVEL) {
            closeEntry()
            heading = block.toEntryHeading()
            body = mutableListOf()

            if (heading == null) {
                AppLog.w("Skipping a changelog section whose heading is not \"<version>: <headline>\"")
            }
        } else if (heading != null) {
            body += block
        }
    }
    closeEntry()

    return Changelog(
        entries = entries.toImmutableList(),
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        source = source,
    )
}

private fun MarkdownBlock.Heading.toEntryHeading(): EntryHeading? {
    val text = spans.joinToString(separator = "") { it.text }
    val version = text.substringBefore(
        delimiter = VERSION_SEPARATOR,
        missingDelimiterValue = "",
    ).trim()
    val headline = text.substringAfter(
        delimiter = VERSION_SEPARATOR,
        missingDelimiterValue = "",
    ).trim()

    return if (version.firstOrNull()?.isDigit() == true && headline.isNotBlank()) {
        EntryHeading(
            version = version,
            headline = headline,
        )
    } else {
        null
    }
}

private fun EntryHeading.toEntry(body: List<MarkdownBlock>): ChangelogEntry {
    val releaseDate = (body.firstOrNull() as? MarkdownBlock.Paragraph)?.spans?.toReleaseDate()

    return ChangelogEntry(
        version = version,
        headline = headline,
        releaseDate = releaseDate,
        blocks = (if (releaseDate != null) body.drop(1) else body).toImmutableList(),
    )
}

private fun List<MarkdownSpan>.toReleaseDate(): LocalDate? {
    if (isEmpty() || all { it.italic }.not()) return null

    return try {
        LocalDate.parse(joinToString(separator = "") { it.text }.trim())
    } catch (_: IllegalArgumentException) {
        null
    }
}
