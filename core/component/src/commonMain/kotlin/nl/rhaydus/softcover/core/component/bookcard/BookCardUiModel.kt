package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * The component fixes every measurement; nothing here sets a size, gap or style:
 * - **Cover** — fills the width its modifier gives it at 2:3; a row's lead width follows
 *   [BookCardVariant.Row.Size].
 * - **Selection** — one treatment on any cover; a coverless row leads with the mark. A selecting card
 *   announces a checkbox with its selected state, not a button.
 * - **Progress** — a wavy bar and its percentage under the cover or the text.
 * - **Text** — resolved and cased by the mapper; the card never formats, upper-cases or computes a
 *   shared-transition key.
 */
@Immutable
data class BookCardUiModel(
    val key: BookCardKey,
    val variant: BookCardVariant,
    val decorations: BookCardDecorations = BookCardDecorations(),
) {
    companion object : UiModelPreviews<BookCardUiModel> {
        override val previews: ImmutableList<BookCardUiModel> = persistentListOf(
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 1),
                variant = BookCardVariant.CoverOnly(cover = cover(title = "Piranesi")),
                decorations = BookCardDecorations(
                    progress = BookCardProgress.Value(
                        fraction = 0.42f,
                        label = "42%",
                    ),
                ),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 21),
                variant = BookCardVariant.CoverOnly(cover = cover(title = "Emma")),
                decorations = BookCardDecorations(progress = BookCardProgress.Reserved),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 22),
                variant = BookCardVariant.CoverOnly(cover = cover(title = "Kindred")),
                decorations = BookCardDecorations(
                    progress = BookCardProgress.Value(
                        fraction = 0.7f,
                        label = null,
                    ),
                ),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 2),
                variant = BookCardVariant.CoverOnly(cover = cover(title = "Dune")),
                decorations = BookCardDecorations(selection = selection(selected = true)),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 3),
                variant = BookCardVariant.CoverOnly(cover = cover(title = "Hyperion")),
                decorations = BookCardDecorations(selection = selection(selected = false)),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 4),
                variant = BookCardVariant.Stacked(
                    cover = cover(title = "The Name of the Wind"),
                    text = BookCardContent(
                        title = "The Name of the Wind",
                        eyebrow = BookCardEyebrow(text = "THE KINGKILLER CHRONICLE"),
                        byline = "By Patrick Rothfuss",
                    ),
                    menuLabel = "More options",
                ),
                decorations = BookCardDecorations(
                    coverBadge = CoverOverlayUiModel(
                        badge = BadgeUiModel(
                            label = "Out Sep 2",
                            tone = BadgeTone.Primary,
                        ),
                        corner = CoverOverlayCorner.TopStart,
                        grayscale = false,
                    ),
                ),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 5),
                variant = BookCardVariant.Stacked(
                    cover = null,
                    text = BookCardContent(
                        title = "Piranesi",
                        supportingLines = persistentListOf(
                            BookCardLine(parts = persistentListOf(BookCardLinePart.Rating(label = "4.5"))),
                        ),
                    ),
                ),
                decorations = BookCardDecorations(
                    progress = BookCardProgress.Value(
                        fraction = 0.6f,
                        label = "60%",
                    ),
                ),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 6),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Compact,
                    lead = BookCardLead.Cover(model = cover(title = "Gideon the Ninth")),
                    text = BookCardContent(
                        title = "Gideon the Ninth",
                        supportingLines = persistentListOf(
                            BookCardLine(
                                parts = persistentListOf(
                                    BookCardLinePart.Text(text = "Tamsyn Muir"),
                                    BookCardLinePart.Rating(label = "4.1"),
                                ),
                            ),
                        ),
                    ),
                    trailing = BookCardTrailing.Bookmark(
                        saved = false,
                        contentDescription = "Save to library",
                    ),
                ),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 7),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Standard,
                    lead = BookCardLead.Cover(model = cover(title = "A Memory Called Empire")),
                    text = BookCardContent(
                        title = "A Memory Called Empire",
                        eyebrow = BookCardEyebrow(
                            text = "COULDN'T SAVE",
                            tone = BookCardEyebrow.Tone.Error,
                        ),
                        byline = "By Arkady Martine",
                    ),
                    trailing = BookCardTrailing.SetProgress(label = "Set progress"),
                    deadline = DeadlineSummaryUiModel(
                        dateText = "28 Aug",
                        paceText = "18 pages/day",
                        tone = DeadlineSummaryTone.OnSurface,
                    ),
                ),
                decorations = BookCardDecorations(
                    progress = BookCardProgress.Value(
                        fraction = 0.3f,
                        label = "30%",
                    ),
                    shakeOnError = true,
                ),
            ),
            BookCardUiModel(
                key = BookCardKey.Book(bookId = 8),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Large,
                    lead = BookCardLead.Cover(model = cover(title = "Project Hail Mary")),
                    text = BookCardContent(
                        title = "Project Hail Mary",
                        byline = "By Andy Weir",
                    ),
                    trailing = BookCardTrailing.Badge(
                        model = BadgeUiModel(
                            label = "Behind",
                            tone = BadgeTone.ErrorContainer,
                            size = BadgeSize.Regular,
                        ),
                    ),
                ),
                decorations = BookCardDecorations(
                    coverBadge = CoverOverlayUiModel(
                        badge = BadgeUiModel(
                            label = "Expired",
                            tone = BadgeTone.Neutral,
                        ),
                        corner = CoverOverlayCorner.TopEnd,
                        grayscale = true,
                    ),
                    pressBehaviour = BookCardDecorations.Press.TapAndLongPress,
                ),
            ),
            BookCardUiModel(
                key = BookCardKey.Series(seriesId = 9),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Standard,
                    lead = BookCardLead.SeriesStack(front = cover(title = "The Stormlight Archive")),
                    text = BookCardContent(
                        title = "The Stormlight Archive",
                        byline = "By Brandon Sanderson",
                    ),
                    trailing = BookCardTrailing.Restore(label = "Restore"),
                ),
                decorations = BookCardDecorations(pressBehaviour = BookCardDecorations.Press.None),
            ),
            BookCardUiModel(
                key = BookCardKey.Edition(editionId = 10),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Compact,
                    lead = null,
                    text = BookCardContent(
                        title = "Piranesi (Audiobook)",
                        titleIcon = SoftcoverIcon.Headset,
                        byline = "By Susanna Clarke",
                    ),
                ),
                decorations = BookCardDecorations(selection = selection(selected = true)),
            ),
        )

        private fun cover(title: String) = CoverUiModel(
            source = CoverSource.Local(
                path = "/fixtures/cover.jpg",
                cacheKey = null,
            ),
            coverlessTitle = title,
            variant = CoverVariant.ExploreRail,
        )

        private fun selection(selected: Boolean) = SelectionMarkUiModel(
            selected = selected,
            backdrop = SelectionMarkBackdrop.Image,
        )
    }
}
