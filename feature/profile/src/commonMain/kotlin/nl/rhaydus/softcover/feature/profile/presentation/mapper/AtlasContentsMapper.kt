package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasContentsRowUiModel

internal fun atlasContentsHeader(): SectionHeaderUiModel {
    val count = AtlasChapter.entries.size

    return SectionHeaderUiModel.Section(
        eyebrow = "In this atlas",
        headline = if (count == 1) "One chapter" else "$count chapters",
    )
}

internal fun atlasContentsRows(): ImmutableList<AtlasContentsRowUiModel> =
    AtlasChapter.entries
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

private fun AtlasChapter.number(): String = when (this) {
    AtlasChapter.SEASONS_AND_RECORDS -> "02"
    AtlasChapter.TEMPORARY -> "99"
}

private fun AtlasChapter.title(): String = when (this) {
    AtlasChapter.SEASONS_AND_RECORDS -> "Seasons and records"
    AtlasChapter.TEMPORARY -> "Temporary"
}

private fun AtlasChapter.caption(): String = when (this) {
    AtlasChapter.SEASONS_AND_RECORDS -> "Pages by month, authors and records."
    AtlasChapter.TEMPORARY -> "Year by year, genres, authors, ratings and favourites."
}
