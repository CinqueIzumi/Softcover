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

/** Numbered from the same order as [atlasContentsRows], so a chapter and its contents row agree. */
internal fun atlasChapterHeader(chapter: AtlasChapter): SectionHeaderUiModel =
    SectionHeaderUiModel.Section(eyebrow = "${chapter.number()} · ${chapter.title()}")

private fun AtlasChapter.number(): String = (ordinal + 1).toString().padStart(
    length = 2,
    padChar = '0',
)

private fun AtlasChapter.title(): String = when (this) {
    AtlasChapter.TEMPORARY -> "Temporary"
}

private fun AtlasChapter.caption(): String = when (this) {
    AtlasChapter.TEMPORARY -> "Year by year, genres, authors, ratings and favourites."
}
