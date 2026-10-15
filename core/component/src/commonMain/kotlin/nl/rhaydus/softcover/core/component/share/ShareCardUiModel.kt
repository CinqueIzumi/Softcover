package nl.rhaydus.softcover.core.component.share

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.richtext.RichTextParagraph
import nl.rhaydus.softcover.core.component.richtext.RichTextRun
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextVariant

/**
 * The content behind [ShareCard]'s six variants: [BookShareCardUiModel] (the plain editorial book
 * card — cover, title, byline, optional rating), [ReadingUpdateShareCardUiModel]
 * (a personalised card attributing the book to the reader), [StatShareCardUiModel] (a `primary`-filled
 * hero numeral), [QuoteShareCardUiModel] (an oversize pulled quote), [YearRecapShareCardUiModel] (a
 * year-in-review recap), and [ReadingLifeShareCardUiModel] (a whole-history recap). [ShareCard]
 * dispatches on the concrete subtype to a private per-variant body.
 */
@Immutable
sealed interface ShareCardUiModel {
    companion object : UiModelPreviews<ShareCardUiModel> {
        override val previews: ImmutableMap<String, ShareCardUiModel> = persistentMapOf(
            "Book" to BookShareCardUiModel(
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = "We Have Always Lived in the Castle",
                    variant = CoverVariant.Floating,
                ),
                title = "We Have Always Lived in the Castle",
                author = "Shirley Jackson",
                statsLine = "★ 4.2 · Your 9/10 · 1962 · 214 pages",
                description = "Merricat Blackwood lives with her sister Constance and their uncle Julian in the family " +
                    "mansion outside the village. A quiet gothic tale of isolation, ritual, and the slow-burning " +
                    "aftermath of a single, devastating poisoning.",
                tags = persistentListOf(
                    ChipUiModel(
                        key = "gothic",
                        label = "Gothic",
                        tone = ChipTone.OverMedia,
                        interaction = ChipInteraction.Inert,
                    ),
                    ChipUiModel(
                        key = "unsettling",
                        label = "Unsettling",
                        tone = ChipTone.OverMedia,
                        interaction = ChipInteraction.Inert,
                    ),
                ),
            ),
            "Book (full text)" to BookShareCardUiModel(
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = "We Have Always Lived in the Castle",
                    variant = CoverVariant.Floating,
                ),
                title = "We Have Always Lived in the Castle",
                author = "Shirley Jackson",
                statsLine = "★ 4.2 · Your 9/10 · 1962 · 214 pages",
                description = "Merricat Blackwood lives with her sister Constance and their uncle Julian in the family " +
                    "mansion outside the village. A quiet gothic tale of isolation, ritual, and the slow-burning " +
                    "aftermath of a single, devastating poisoning. The villagers keep their distance, and the " +
                    "sisters keep their routines, until a cousin arrives and the fragile order of the house begins " +
                    "to come apart, one careful ritual at a time.",
                format = ShareCardFormat.FullText,
            ),
            "Book (minimal)" to BookShareCardUiModel(
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = "Piranesi",
                    variant = CoverVariant.Floating,
                ),
                title = "Piranesi",
                author = "Susanna Clarke",
                statsLine = null,
                description = null,
            ),
            "Reading update (finished)" to ReadingUpdateShareCardUiModel.Finished(
                username = "cinque",
                avatarUrl = null,
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = "The Name of the Wind",
                    variant = CoverVariant.Floating,
                ),
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                tags = persistentListOf(
                    ChipUiModel(
                        key = "slow-burn",
                        label = "Slow burn",
                        tone = ChipTone.OverMedia,
                        interaction = ChipInteraction.Inert,
                    ),
                    ChipUiModel(
                        key = "favourites",
                        label = "Favourites",
                        tone = ChipTone.OverMedia,
                        interaction = ChipInteraction.Inert,
                    ),
                ),
                ratingStars = 4.5,
                review = RichTextUiModel(
                    paragraphs = persistentListOf(
                        RichTextParagraph(
                            runs = persistentListOf(
                                RichTextRun(text = "A gorgeous, immersive tale that pulls you into Kvothe's world. The ending — "),
                                RichTextRun(
                                    text = "he was the villain all along",
                                    spoiler = true,
                                ),
                                RichTextRun(text = " — still floored me."),
                            ),
                        ),
                    ),
                ),
            ),
            "Reading update (finished, full text)" to ReadingUpdateShareCardUiModel.Finished(
                username = "cinque",
                avatarUrl = null,
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = "The Name of the Wind",
                    variant = CoverVariant.Floating,
                ),
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                tags = persistentListOf(
                    ChipUiModel(
                        key = "slow-burn",
                        label = "Slow burn",
                        tone = ChipTone.OverMedia,
                        interaction = ChipInteraction.Inert,
                    ),
                ),
                format = ShareCardFormat.FullText,
                ratingStars = 4.5,
                review = RichTextUiModel(
                    variant = RichTextVariant.ShareCardFullText,
                    paragraphs = persistentListOf(
                        RichTextParagraph(
                            runs = persistentListOf(
                                RichTextRun(text = "A gorgeous, immersive tale that pulls you into Kvothe's world, told "),
                                RichTextRun(text = "with patience and a love of language that few fantasy novels manage. The ending, "),
                                RichTextRun(
                                    text = "he was the villain all along",
                                    spoiler = true,
                                ),
                                RichTextRun(text = ", still floored me."),
                            ),
                        ),
                        RichTextParagraph(
                            runs = persistentListOf(
                                RichTextRun(text = "I kept coming back to the quiet scenes in the inn, the long talks and the slow "),
                                RichTextRun(text = "reveal of how a legend gets built from small choices."),
                            ),
                        ),
                    ),
                ),
            ),
            "Reading update (in progress)" to ReadingUpdateShareCardUiModel.Reading(
                username = "cinque",
                avatarUrl = null,
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = "The Name of the Wind",
                    variant = CoverVariant.Floating,
                ),
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                tags = persistentListOf(),
                progressLabel = "page 212 / 662 · 32%",
            ),
            "Stat" to StatShareCardUiModel(
                eyebrow = "Pages read in 2026",
                value = 8_402L,
                caption = "across 14 books, mostly on Sunday afternoons.",
            ),
            "Quote" to QuoteShareCardUiModel(
                quote = "The reader is the protagonist; the page is the stage.",
                sourceTitle = "Piranesi",
                sourceAuthor = "Susanna Clarke",
                page = 142,
            ),
            "Year recap" to YearRecapShareCardUiModel(
                eyebrow = "Year in Books",
                year = 2026,
                headline = "A year of long evenings.",
                highlights = persistentListOf(
                    "24 finished, 8,402 pages.",
                    "Top genre: literary fiction.",
                    "Longest haul: Lonesome Dove.",
                ),
            ),
            "Reading life" to ReadingLifeShareCardUiModel(
                readerName = "Elena Marchetti",
                avatarUrl = null,
                readerInitials = "EM",
                totalPagesRead = "128,406",
                totalBooksLine = "across 214 books",
                topGenres = persistentListOf(
                    ReadingLifeGenre(
                        name = "Literary fiction",
                        percentLabel = "34%",
                    ),
                    ReadingLifeGenre(
                        name = "Mystery",
                        percentLabel = "21%",
                    ),
                    ReadingLifeGenre(
                        name = "Science fiction",
                        percentLabel = "14%",
                    ),
                ),
                pagesByMonth = persistentListOf(3_400, 2_100, 4_200, 3_900, 5_100, 4_800, 3_300, 2_900, 4_600, 5_300, 4_100, 3_800),
                averageRating = 4.2,
                averageRatingText = "4.2",
                dayStreak = "31",
                trackedYears = "8",
            ),
        )
    }
}
