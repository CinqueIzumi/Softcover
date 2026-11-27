package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasContentsRowUiModel

/** [chapters] are the ones the scope shows; the headline counts them. */
internal fun atlasContentsHeader(chapters: List<AtlasChapter> = AtlasChapter.entries): SectionHeaderUiModel {
    val count = chapters.size

    return SectionHeaderUiModel.Section(
        eyebrow = "In this atlas",
        headline = if (count == 1) "One chapter" else "$count chapters",
    )
}

internal fun atlasContentsRows(
    chapters: List<AtlasChapter> = AtlasChapter.entries,
): ImmutableList<AtlasContentsRowUiModel> =
    chapters
        .map { chapter ->
            AtlasContentsRowUiModel(
                chapter = chapter,
                number = chapter.number(),
                title = chapter.title(),
                caption = chapter.caption(),
            )
        }
        .toImmutableList()

/** Numbered like [atlasContentsRows], so a chapter and its contents row agree. */
internal fun atlasChapterHeader(chapter: AtlasChapter): SectionHeaderUiModel =
    SectionHeaderUiModel.Section(eyebrow = atlasChapterEyebrow(chapter))

/** The "02 · Seasons and records" line a chapter's header opens with, so a header and its contents row agree. */
internal fun atlasChapterEyebrow(chapter: AtlasChapter): String = "${chapter.number()} · ${chapter.title()}"

// Fixed per chapter from the design's chapter list, not by position, so a gap is a chapter the atlas
// does not have yet.
private fun AtlasChapter.number(): String = when (this) {
    AtlasChapter.YEAR_AT_A_GLANCE -> "01"
    AtlasChapter.SEASONS_AND_RECORDS -> "02"
    AtlasChapter.GENRES_AND_HOW_YOU_READ -> "03"
    AtlasChapter.YOUR_READING_LIST -> "05"
    AtlasChapter.IN_YOUR_WORDS -> "06"
    AtlasChapter.RECOMMENDERS_AND_SPENDING -> "07"
    AtlasChapter.DIVERSITY_AND_PUBLICATION -> "08"
    AtlasChapter.TEMPORARY -> "99"
}

private fun AtlasChapter.title(): String = when (this) {
    AtlasChapter.YEAR_AT_A_GLANCE -> "A year at a glance"
    AtlasChapter.SEASONS_AND_RECORDS -> "Seasons and records"
    AtlasChapter.GENRES_AND_HOW_YOU_READ -> "Genres and how you read"
    AtlasChapter.YOUR_READING_LIST -> "Your reading list"
    AtlasChapter.IN_YOUR_WORDS -> "In your words"
    AtlasChapter.RECOMMENDERS_AND_SPENDING -> "Recommenders and spending"
    AtlasChapter.DIVERSITY_AND_PUBLICATION -> "Diversity and publication"
    AtlasChapter.TEMPORARY -> "Temporary"
}

private fun AtlasChapter.caption(): String = when (this) {
    AtlasChapter.YEAR_AT_A_GLANCE -> "Every day you read, and when."
    AtlasChapter.SEASONS_AND_RECORDS -> "Pages by month, authors and records."
    AtlasChapter.GENRES_AND_HOW_YOU_READ -> "What you reach for, and how it reads."
    AtlasChapter.YOUR_READING_LIST -> "What waits, and what you set aside."
    AtlasChapter.IN_YOUR_WORDS -> "Reviews, quotes and your own tags."
    AtlasChapter.RECOMMENDERS_AND_SPENDING -> "Whose picks you read, and the cost."
    AtlasChapter.DIVERSITY_AND_PUBLICATION -> "Whose stories, which languages, when."
    AtlasChapter.TEMPORARY -> "Year by year, ratings and favourites."
}
