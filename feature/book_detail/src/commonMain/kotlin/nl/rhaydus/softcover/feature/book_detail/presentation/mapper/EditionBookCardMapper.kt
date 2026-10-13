package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel

/**
 * @param fallbackEdition Supplies the cover for an edition with no cover of its own.
 */
internal fun BookEdition.toEditionCard(
    fallbackEdition: BookEdition?,
    selected: Boolean,
    tracked: Boolean,
): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Edition(editionId = id),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Standard,
        lead = BookCardLead.Cover(
            cover = BookCardCover(
                model = toCoverUiModel(
                    defaultEdition = fallbackEdition,
                    coverlessTitle = title,
                    variant = CoverVariant.Flat,
                ),
                badge = OWNED_COVER_BADGE.takeIf { owned },
            ),
        ),
        text = BookCardContent(
            title = formatLabel(),
            titleIcon = readingFormat.formatGlyph(),
            supportingLines = supportingLines(),
        ),
        trailing = TRACKING_TRAILING.takeIf { tracked },
    ),
    decorations = BookCardDecorations(selection = BookCardSelection.Single(selected = selected)),
)

private val TRACKING_TRAILING = BookCardTrailing.Badge(
    model = BadgeUiModel(
        label = "TRACKING NOW",
        tone = BadgeTone.Neutral,
        size = BadgeSize.Regular,
    ),
)

private val OWNED_COVER_BADGE = CoverOverlayUiModel(
    badge = ownedBadge(size = BadgeSize.Compact),
    corner = CoverOverlayCorner.TopEnd,
    grayscale = false,
)

private fun BookEdition.supportingLines() = buildList {
    metaLine()
        .takeIf { it.isNotEmpty() }
        ?.let { add(it) }

    isbnWhisper()
        ?.let { add(persistentListOf(BookCardLinePart.Text(text = it))) }
}.toImmutableList()

/** The format glyph that leads each row; `readingFormat` maps to a single icon. */
private fun ReadingFormat?.formatGlyph(): SoftcoverIcon = when (this) {
    ReadingFormat.Audio -> SoftcoverIcon.Headset
    ReadingFormat.Ebook -> SoftcoverIcon.LibraryBooks
    ReadingFormat.Physical, ReadingFormat.Both, null -> SoftcoverIcon.MenuBook
}

private fun BookEdition.formatLabel(): String =
    format.takeIf { it.isNotBlank() } ?: readingFormat?.label ?: "Edition"

/**
 * Publisher, release year and pages as separate parts, with pages swapped for a duration ("{h}h {m}m") on an audiobook.
 * Unknown parts are dropped rather than shown as a placeholder.
 */
private fun BookEdition.metaLine(): ImmutableList<BookCardLinePart> {
    val parts = buildList {
        publisher?.takeIf { it.isNotBlank() }?.let(::add)
        releaseYear.takeIf { it != -1 }?.let { add(it.toString()) }

        if (isAudiobook) {
            audioSeconds?.let { seconds ->
                val duration = seconds.toHoursMinutesSeconds()
                add("${duration.hours}h ${duration.minutes}m")
            }
        } else {
            pages?.let { add("$it pp") }
        }
    }

    return parts.map { BookCardLinePart.Text(text = it) }.toImmutableList()
}

private fun BookEdition.isbnWhisper(): String? {
    val isbn = isbn13?.takeIf { it.isNotBlank() } ?: isbn10?.takeIf { it.isNotBlank() }

    return isbn?.let { "ISBN $it" }
}

/** The edition selector's "N EDITIONS" label for [count] cards. */
internal fun editionCountHeaderFor(count: Int): SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "$count EDITIONS")
