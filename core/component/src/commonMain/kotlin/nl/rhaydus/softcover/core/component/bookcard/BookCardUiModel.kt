package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
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
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * The component fixes every measurement; the only dp a model carries are [BookCardVariant.Row]'s content insets:
 * - **Cover** — fills the width its modifier gives it at 2:3; a row's lead width follows
 *   [BookCardVariant.Row.Size].
 * - **Selection** — one treatment on any cover; a coverless row leads with the mark. A selecting card
 *   announces its selected state, not a button.
 * - **Progress** — a wavy bar and its percentage under the cover or the text.
 * - **Text** — resolved and cased by the mapper; the card never formats, upper-cases or computes a
 *   shared-transition key.
 *
 * @property isLoading The card lays out the model as given with every leaf masked, so a stand-in model sizes
 * the slot the real one will fill. Its trailing control is built disabled, which is what keeps it silent.
 */
@Immutable
data class BookCardUiModel(
    val key: BookCardKey,
    val variant: BookCardVariant,
    val decorations: BookCardDecorations = BookCardDecorations(),
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<BookCardUiModel> {
        private val LOADING_COVER_ONLY = BookCardUiModel(
            key = BookCardKey.Book(bookId = 31),
            variant = BookCardVariant.CoverOnly(cover = BookCardCover(model = loadingCover())),
            decorations = BookCardDecorations(
                progress = BookCardProgress.Value(
                    fraction = 0.5f,
                    label = "50%",
                ),
            ),
            isLoading = true,
        )

        private val LOADING_COLUMN = BookCardUiModel(
            key = BookCardKey.Book(bookId = 32),
            variant = BookCardVariant.Column(
                cover = BookCardCover(model = loadingCover()),
                text = BookCardContent(
                    title = "Loading title",
                    eyebrow = BookCardEyebrow(text = "SERIES NAME"),
                    byline = "By Author Name",
                ),
            ),
            isLoading = true,
        )

        private val LOADING_ROW = BookCardUiModel(
            key = BookCardKey.Book(bookId = 33),
            variant = BookCardVariant.Row(
                size = BookCardVariant.Row.Size.Standard,
                lead = BookCardLead.Cover(cover = BookCardCover(model = loadingCover())),
                text = BookCardContent(
                    title = "Loading title",
                    byline = "By Author Name",
                ),
            ),
            isLoading = true,
        )

        private val LOADING_SERIES_ROW = BookCardUiModel(
            key = BookCardKey.Series(seriesId = 34),
            variant = BookCardVariant.Row(
                size = BookCardVariant.Row.Size.Standard,
                lead = BookCardLead.SeriesStack(front = loadingCover()),
                text = BookCardContent(
                    title = "Loading series",
                    byline = "By Author Name",
                ),
            ),
            isLoading = true,
        )

        override val previews: ImmutableMap<String, BookCardUiModel> = persistentMapOf(
            "Cover only, progress" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 1),
                variant = BookCardVariant.CoverOnly(cover = bookCover(title = "Piranesi")),
                decorations = BookCardDecorations(
                    progress = BookCardProgress.Value(
                        fraction = 0.42f,
                        label = "42%",
                    ),
                ),
            ),
            "Cover only, progress reserved" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 21),
                variant = BookCardVariant.CoverOnly(cover = bookCover(title = "Emma")),
                decorations = BookCardDecorations(progress = BookCardProgress.Reserved),
            ),
            "Cover only, progress without label" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 22),
                variant = BookCardVariant.CoverOnly(cover = bookCover(title = "Kindred")),
                decorations = BookCardDecorations(
                    progress = BookCardProgress.Value(
                        fraction = 0.7f,
                        label = null,
                    ),
                ),
            ),
            "Cover only, selected" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 2),
                variant = BookCardVariant.CoverOnly(cover = bookCover(title = "Dune")),
                decorations = BookCardDecorations(selection = BookCardSelection.Multiple(selected = true)),
            ),
            "Cover only, unselected" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 3),
                variant = BookCardVariant.CoverOnly(cover = bookCover(title = "Hyperion")),
                decorations = BookCardDecorations(selection = BookCardSelection.Multiple(selected = false)),
            ),
            "Column, cover badge" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 4),
                variant = BookCardVariant.Column(
                    cover = bookCover(
                        title = "The Name of the Wind",
                        badge = CoverOverlayUiModel(
                            badge = BadgeUiModel(
                                label = "Out Sep 2",
                                tone = BadgeTone.Primary,
                            ),
                            corner = CoverOverlayCorner.TopStart,
                            grayscale = false,
                        ),
                    ),
                    text = BookCardContent(
                        title = "The Name of the Wind",
                        eyebrow = BookCardEyebrow(text = "THE KINGKILLER CHRONICLE"),
                        byline = "By Patrick Rothfuss",
                    ),
                    hasMenu = true,
                ),
            ),
            "Column, progress" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 5),
                variant = BookCardVariant.Column(
                    cover = null,
                    text = BookCardContent(
                        title = "Piranesi",
                        supportingLines = persistentListOf(
                            persistentListOf(
                                BookCardLinePart.Rating(
                                    label = "4.5",
                                    contentDescription = "Rated 4.5 out of 5",
                                ),
                            ),
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
            "Row Compact, Bookmark" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 6),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Compact,
                    lead = BookCardLead.Cover(cover = bookCover(title = "Gideon the Ninth")),
                    text = BookCardContent(
                        title = "Gideon the Ninth",
                        supportingLines = persistentListOf(
                            persistentListOf(
                                BookCardLinePart.Text(text = "Tamsyn Muir"),
                                BookCardLinePart.Rating(
                                    label = "4.1",
                                    contentDescription = "Rated 4.1 out of 5",
                                ),
                            ),
                        ),
                    ),
                    trailing = BookCardTrailing.Bookmark.labelled(
                        saved = false,
                        contentDescription = "Save to library",
                    ),
                ),
            ),
            "Row Standard, progress, deadline, SetProgress" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 7),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Standard,
                    lead = BookCardLead.Cover(cover = bookCover(title = "A Memory Called Empire")),
                    text = BookCardContent(
                        title = "A Memory Called Empire",
                        eyebrow = BookCardEyebrow(
                            text = "COULDN'T SAVE",
                            tone = BookCardEyebrow.Tone.Error,
                        ),
                        byline = "By Arkady Martine",
                    ),
                    trailing = BookCardTrailing.SetProgress.labelled(),
                    deadline = DeadlineSummaryUiModel(
                        text = "28 Aug • 18 pages/day",
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
            "Row Large, cover badge, Badge" to BookCardUiModel(
                key = BookCardKey.Book(bookId = 8),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Large,
                    lead = BookCardLead.Cover(
                        cover = bookCover(
                            title = "Project Hail Mary",
                            badge = CoverOverlayUiModel(
                                badge = BadgeUiModel(
                                    label = "Expired",
                                    tone = BadgeTone.Neutral,
                                ),
                                corner = CoverOverlayCorner.TopEnd,
                                grayscale = true,
                            ),
                        ),
                    ),
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
                decorations = BookCardDecorations(pressBehaviour = BookCardDecorations.Press.TapAndLongPress),
            ),
            "Row Standard, series stack, Restore" to BookCardUiModel(
                key = BookCardKey.Series(seriesId = 9),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Standard,
                    lead = BookCardLead.SeriesStack(front = cover(title = "The Stormlight Archive")),
                    text = BookCardContent(
                        title = "The Stormlight Archive",
                        byline = "By Brandon Sanderson",
                    ),
                    trailing = BookCardTrailing.Restore,
                ),
            ),
            "Row Compact, selected, coverless" to BookCardUiModel(
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
                decorations = BookCardDecorations(selection = BookCardSelection.Multiple(selected = true)),
            ),
            "Row Standard, single choice, selected" to BookCardUiModel(
                key = BookCardKey.Edition(editionId = 11),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Standard,
                    lead = BookCardLead.Cover(cover = bookCover(title = "Piranesi (Hardcover)")),
                    text = BookCardContent(
                        title = "Hardcover",
                        titleIcon = SoftcoverIcon.MenuBook,
                        byline = "Bloomsbury",
                    ),
                ),
                decorations = BookCardDecorations(selection = BookCardSelection.Single(selected = true)),
            ),
            "Row Standard, single choice, unselected" to BookCardUiModel(
                key = BookCardKey.Edition(editionId = 12),
                variant = BookCardVariant.Row(
                    size = BookCardVariant.Row.Size.Standard,
                    lead = BookCardLead.Cover(cover = bookCover(title = "Piranesi (Paperback)")),
                    text = BookCardContent(
                        title = "Paperback",
                        titleIcon = SoftcoverIcon.MenuBook,
                        byline = "Bloomsbury",
                    ),
                ),
                decorations = BookCardDecorations(selection = BookCardSelection.Single(selected = false)),
            ),
            "Cover only, loading, progress" to LOADING_COVER_ONLY,
            "Column, loading" to LOADING_COLUMN,
            "Row Standard, loading" to LOADING_ROW,
            "Row Standard, loading, series stack" to LOADING_SERIES_ROW,
        )

        private fun bookCover(
            title: String,
            badge: CoverOverlayUiModel? = null,
        ) = BookCardCover(
            model = cover(title = title),
            badge = badge,
        )

        private fun loadingCover() = CoverUiModel(
            source = null,
            coverlessTitle = null,
            variant = CoverVariant.Raised,
            isLoading = true,
        )

        private fun cover(title: String) = CoverUiModel(
            source = CoverSource.Local(
                path = "/fixtures/cover.jpg",
                cacheKey = null,
            ),
            coverlessTitle = title,
            variant = CoverVariant.Raised,
        )
    }
}
