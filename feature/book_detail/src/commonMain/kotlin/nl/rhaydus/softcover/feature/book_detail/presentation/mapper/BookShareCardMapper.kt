package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardFormat
import nl.rhaydus.softcover.core.component.verdict.verdictScoreOf
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import kotlin.math.roundToInt

internal fun Book.toBookShareCardUiModel(
    edition: BookEdition?,
    format: ShareCardFormat,
    selectedTags: Set<String> = emptySet(),
): BookShareCardUiModel {
    val resolvedEdition = edition ?: defaultEdition

    return BookShareCardUiModel(
        cover = resolvedEdition.toCoverUiModel(
            defaultEdition = defaultEdition,
            coverlessTitle = title,
            variant = CoverVariant.Floating,
            fallbackCoverUrl = coverUrl.takeIf { it.isNotBlank() },
        ),
        title = title,
        author = authors.firstOrNull()?.name.orEmpty(),
        statsLine = statsLineOf(
            communityRating = rating.takeIf { it > 0.0 },
            userRating = verdictScoreOf(rating = userBook?.rating?.takeIf { it > 0.0 }),
            releaseYear = resolvedEdition?.releaseYear?.takeIf { it != -1 } ?: releaseYear.takeIf { it != -1 },
            pageCount = resolvedEdition?.pages,
        ),
        description = description.takeIf { it.isNotBlank() },
        tags = shareTagCandidates()
            .values
            .flatten()
            .filter { it in selectedTags }
            .map { name ->
                ChipUiModel(
                    key = name,
                    label = name,
                    tone = ChipTone.OverMedia,
                    interaction = ChipInteraction.Inert,
                )
            }
            .toImmutableList(),
        format = format,
    )
}

private fun statsLineOf(
    communityRating: Double?,
    userRating: Int?,
    releaseYear: Int?,
    pageCount: Int?,
): String? {
    val parts = buildList {
        communityRating?.let {
            val tenths = (it * 10).roundToInt()

            add("★ ${tenths / 10}.${tenths % 10}")
        }
        userRating?.let { add("Your $it/10") }
        releaseYear?.let { add(it.toString()) }
        pageCount?.takeIf { it > 0 }?.let { add("$it pages") }
    }

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
}
