package nl.rhaydus.softcover.core.component.share

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.richtext.RichTextParagraph
import nl.rhaydus.softcover.core.component.richtext.RichTextRun
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

sealed interface ShareCardUiModel {
    companion object : UiModelPreviews<ShareCardUiModel> {
        /**
         * Per R5, one fixture per variant branch, plus two that change the card's anatomy rather
         * than just its strings: a book with almost every optional field null, and the in-progress
         * reading update, which swaps the rating + review pair for a progress label.
         *
         * Every `@Preview` in the family's body files renders off this list rather than
         * re-declaring sample literals inline, so the preview set and the Component Gallery's set
         * cannot drift apart.
         */
        override val previews: ImmutableList<ShareCardUiModel> = persistentListOf(
            BookShareCardUiModel(
                coverUrl = null,
                title = "We Have Always Lived in the Castle",
                author = "Shirley Jackson",
                communityRating = 4.2,
                userRating = 9,
                releaseYear = 1962,
                pageCount = 214,
                description = "Merricat Blackwood lives with her sister Constance and their uncle Julian in the family " +
                    "mansion outside the village. A quiet gothic tale of isolation, ritual, and the slow-burning " +
                    "aftermath of a single, devastating poisoning.",
                quote = "Merricat, said Connie, would you like a cup of tea?",
            ),
            BookShareCardUiModel(
                coverUrl = null,
                title = "Piranesi",
                author = "Susanna Clarke",
                communityRating = null,
                userRating = null,
                releaseYear = null,
                pageCount = null,
                description = null,
                quote = null,
            ),
            ReadingUpdateShareCardUiModel(
                username = "cinque",
                avatarUrl = null,
                coverUrl = null,
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                kind = ReadingUpdateKind.FINISHED,
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
                progressLabel = null,
            ),
            ReadingUpdateShareCardUiModel(
                username = "cinque",
                avatarUrl = null,
                coverUrl = null,
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                kind = ReadingUpdateKind.READING,
                ratingStars = null,
                review = null,
                progressLabel = "page 212 / 662 · 32%",
            ),
            StatShareCardUiModel(
                eyebrow = "Pages read in 2026",
                value = 8_402L,
                caption = "across 14 books, mostly on Sunday afternoons.",
            ),
            QuoteShareCardUiModel(
                quote = "The reader is the protagonist; the page is the stage.",
                sourceTitle = "Piranesi",
                sourceAuthor = "Susanna Clarke",
                page = 142,
            ),
            YearRecapShareCardUiModel(
                eyebrow = "Year in Books",
                year = 2026,
                headline = "A year of long evenings.",
                highlights = persistentListOf(
                    "24 finished, 8,402 pages.",
                    "Top genre: literary fiction.",
                    "Longest haul: Lonesome Dove.",
                ),
            ),
            ReadingLifeShareCardUiModel(
                readerName = "Elena Marchetti",
                avatarUrl = null,
                totalPagesRead = 128_406,
                totalBooksRead = 214,
                topGenres = persistentListOf(
                    ReadingLifeGenre(
                        name = "Literary fiction",
                        percentage = 34,
                    ),
                    ReadingLifeGenre(
                        name = "Mystery",
                        percentage = 21,
                    ),
                    ReadingLifeGenre(
                        name = "Science fiction",
                        percentage = 14,
                    ),
                ),
                pagesByMonth = persistentListOf(3_400, 2_100, 4_200, 3_900, 5_100, 4_800, 3_300, 2_900, 4_600, 5_300, 4_100, 3_800),
                averageRating = 4.2,
                dayStreak = 31,
                trackedYears = 8,
            ),
        )
    }
}
