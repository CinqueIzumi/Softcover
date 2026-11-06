package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.quote.QuoteCardUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.MostQuotedBook
import nl.rhaydus.softcover.core.profile.domain.model.WrittenStats
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.InYourWordsUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.MostQuotedUiModel

private const val REVIEWS_EYEBROW = "Reviews"
private const val QUOTES_EYEBROW = "Quotes"
private const val MOST_QUOTED_TITLE = "Your most-quoted book"
private const val STAND_IN_WORDS_CAPTION = "0,000 words in all"
private const val STAND_IN_QUOTES_CAPTION = "saved from 00 books"
private const val STAND_IN_MOST_QUOTED_CAPTION = "You saved 00 lines from Loading title. This one first."

/** A null [written] yields the masked stand-in. */
internal fun inYourWordsUiModel(
    written: WrittenStats?,
    scope: StatsScope,
): InYourWordsUiModel {
    if (written == null) return loadingInYourWords()

    val phrase = scope.scopePhrase()

    return InYourWordsUiModel(
        header = inYourWordsHeader(),
        tiles = persistentListOf(
            countTile(
                eyebrow = REVIEWS_EYEBROW,
                count = written.reviewCount,
                caption = if (written.reviewCount == 0) {
                    "None written ${phrase ?: "yet"}"
                } else {
                    "${formatGroupedNumber(written.reviewWordCount)} ${plural(
                        written.reviewWordCount,
                        "word",
                    )} in all"
                },
                isLoading = false,
            ),
            countTile(
                eyebrow = QUOTES_EYEBROW,
                count = written.quoteCount,
                caption = if (written.quoteCount == 0) {
                    "None saved ${phrase ?: "yet"}"
                } else {
                    "saved from ${written.quotedBookCount} ${plural(
                        written.quotedBookCount,
                        "book",
                    )}"
                },
                isLoading = false,
            ),
        ),
        mostQuoted = written.mostQuoted?.toUiModel(),
        hasContent = written.reviewCount > 0 || written.quoteCount > 0,
        isLoading = false,
    )
}

private fun inYourWordsHeader(): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = atlasChapterEyebrow(AtlasChapter.IN_YOUR_WORDS),
    headline = "What you wrote",
    description = "The reviews you left and the lines you kept.",
)

private fun plural(
    count: Int,
    noun: String,
): String = if (count == 1) noun else "${noun}s"

private fun countTile(
    eyebrow: String,
    count: Int,
    caption: String,
    isLoading: Boolean,
): StatTileUiModel = StatTileUiModel(
    eyebrow = eyebrow,
    stat = StatNumberUiModel(
        value = count.toDouble(),
        format = StatNumberFormat.Grouped,
        isLoading = isLoading,
    ),
    variant = StatTileVariant.Standard(caption = caption),
)

private fun MostQuotedBook.toUiModel(): MostQuotedUiModel {
    val lines = "$quoteCount ${plural(
        quoteCount,
        "line",
    )}"

    val quotes = "$quoteCount ${plural(
        quoteCount,
        "quote",
    )}"

    val author = book.primaryAuthors.firstOrNull()?.name

    return MostQuotedUiModel(
        title = MOST_QUOTED_TITLE,
        caption = if (quoteCount == 1) {
            "You saved $lines from ${book.title}."
        } else {
            "You saved $lines from ${book.title}. This one first."
        },
        card = QuoteCardUiModel.Book(
            text = firstQuote.text,
            cover = book.toCoverUiModel(variant = CoverVariant.Flat),
            title = book.title,
            caption = author?.let { "$it · $quotes" } ?: quotes,
        ),
        bookId = book.id,
    )
}

private fun loadingInYourWords(): InYourWordsUiModel = InYourWordsUiModel(
    header = inYourWordsHeader(),
    tiles = persistentListOf(
        countTile(
            eyebrow = REVIEWS_EYEBROW,
            count = 0,
            caption = STAND_IN_WORDS_CAPTION,
            isLoading = true,
        ),
        countTile(
            eyebrow = QUOTES_EYEBROW,
            count = 0,
            caption = STAND_IN_QUOTES_CAPTION,
            isLoading = true,
        ),
    ),
    mostQuoted = MostQuotedUiModel(
        title = MOST_QUOTED_TITLE,
        caption = STAND_IN_MOST_QUOTED_CAPTION,
        card = QuoteCardUiModel.Book(
            text = "Loading quote text that stands in for the line you saved first.",
            cover = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = CoverVariant.Flat,
                isLoading = true,
            ),
            title = "Loading title",
            caption = "Loading author · 00 quotes",
            isLoading = true,
        ),
        bookId = -1,
    ),
    hasContent = true,
    isLoading = true,
)
