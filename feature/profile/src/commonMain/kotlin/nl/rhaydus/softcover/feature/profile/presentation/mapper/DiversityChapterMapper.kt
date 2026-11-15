package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartSliceUiModel
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.AuthorshipGender
import nl.rhaydus.softcover.core.profile.domain.model.CharacterBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.CharacterRepresentation
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.DiversityStats
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.DemographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.DiversityChapterUiModel

private const val LOADING_SEGMENT_COUNT = 3
private const val GENDER_LABEL = "Gender"
private const val AUTHOR_BIPOC_LABEL = "Authors of colour"
private const val AUTHOR_LGBTQ_LABEL = "LGBTQ+ authors"
private const val CHARACTER_BIPOC_LABEL = "BIPOC characters"
private const val CHARACTER_LGBTQ_LABEL = "LGBTQ+ characters"
private const val CHARACTER_SOURCE_CAPTION = "From your own How it read answers."
private const val SEPARATOR = " · "

internal fun diversityChapterUiModel(
    stats: DiversityStats?,
    hideBooksWithoutDetail: Boolean,
    scope: StatsScope = StatsScope.AllTime,
): DiversityChapterUiModel {
    val header = diversityChapterHeader(hideBooksWithoutDetail)

    if (stats == null) return loadingDiversityChapter(header)

    val authors = stats.authorDemographics.let {
        if (hideBooksWithoutDetail) it.excludingBooksWithoutDetail() else it
    }

    val characters = stats.characterRepresentation.let {
        if (hideBooksWithoutDetail) it.excludingNotAnswered() else it
    }

    return DiversityChapterUiModel(
        header = header,
        genderBreakdown = genderBreakdownUiModel(
            demographics = authors,
            hideBooksWithoutDetail = hideBooksWithoutDetail,
        ),
        authorBipoc = authorBreakdownUiModel(
            label = AUTHOR_BIPOC_LABEL,
            breakdown = authors.bipocBreakdown,
            hideBooksWithoutDetail = hideBooksWithoutDetail,
        ),
        authorLgbtq = authorBreakdownUiModel(
            label = AUTHOR_LGBTQ_LABEL,
            breakdown = authors.lgbtqBreakdown,
            hideBooksWithoutDetail = hideBooksWithoutDetail,
        ),
        characterBipoc = characterBreakdownUiModel(
            label = CHARACTER_BIPOC_LABEL,
            breakdown = characters.bipoc,
            hideBooksWithoutDetail = hideBooksWithoutDetail,
        ),
        characterLgbtq = characterBreakdownUiModel(
            label = CHARACTER_LGBTQ_LABEL,
            breakdown = characters.lgbtq,
            hideBooksWithoutDetail = hideBooksWithoutDetail,
        ),
        scopeCaption = diversityScopeCaption(
            hideBooksWithoutDetail = hideBooksWithoutDetail,
            scope = scope,
        ),
        characterSourceCaption = CHARACTER_SOURCE_CAPTION,
        emptyCaption = diversityEmptyCaption(
            hideBooksWithoutDetail = hideBooksWithoutDetail,
            booksRead = stats.booksRead,
            scope = scope,
        ),
        canHideBooksWithoutDetail = stats.authorDemographics.hasUnknownBooks() ||
            stats.characterRepresentation.hasUnansweredBooks(),
    )
}

internal fun genderBreakdownUiModel(
    demographics: AuthorDemographics?,
    hideBooksWithoutDetail: Boolean,
): DemographicBreakdownUiModel = DemographicBreakdownUiModel(
    label = GENDER_LABEL,
    chart = genderChartUiModel(demographics?.genderSlices),
    caption = genderCaption(
        bookCount = (demographics?.knownGenderCount ?: 0) + (demographics?.unknownGenderCount ?: 0),
        unknownCount = demographics?.unknownGenderCount ?: 0,
        hideBooksWithoutDetail = hideBooksWithoutDetail,
    ),
    hasData = demographics?.genderSlices.orEmpty().isNotEmpty(),
)

internal fun authorBreakdownUiModel(
    label: String,
    breakdown: DemographicBreakdown?,
    hideBooksWithoutDetail: Boolean,
): DemographicBreakdownUiModel = DemographicBreakdownUiModel(
    label = label,
    chart = authorChartUiModel(breakdown),
    caption = authorCaption(
        breakdown = breakdown ?: DemographicBreakdown(),
        hideBooksWithoutDetail = hideBooksWithoutDetail,
    ),
    hasData = (breakdown?.total ?: 0) > 0,
)

internal fun characterBreakdownUiModel(
    label: String,
    breakdown: CharacterBreakdown?,
    hideBooksWithoutDetail: Boolean,
): DemographicBreakdownUiModel = DemographicBreakdownUiModel(
    label = label,
    chart = characterChartUiModel(breakdown),
    caption = characterCaption(
        breakdown = breakdown ?: CharacterBreakdown(),
        hideBooksWithoutDetail = hideBooksWithoutDetail,
    ),
    hasData = (breakdown?.total ?: 0) > 0,
)

internal fun diversityScopeCaption(
    hideBooksWithoutDetail: Boolean,
    scope: StatsScope = StatsScope.AllTime,
): String {
    if (hideBooksWithoutDetail) return "Each bar counts only the books with that detail."

    val phrase = scope.scopePhrase()?.let { " $it" }.orEmpty()

    return "Shares of every book you read$phrase, with or without the detail."
}

internal fun diversityEmptyCaption(
    hideBooksWithoutDetail: Boolean,
    booksRead: Int,
    scope: StatsScope = StatsScope.AllTime,
): String {
    if (booksRead == 0) {
        return scope.scopePhrase()?.let { "No books read $it." } ?: "No books read yet."
    }

    return if (hideBooksWithoutDetail) {
        "None of your books carry this detail yet. Turn off Hide books without this detail to see them."
    } else {
        "This fills in as your authors get tagged on Hardcover and you answer How it read."
    }
}

internal fun genderChartUiModel(slices: List<GenderSlice>?): ProportionChartUiModel {
    if (slices == null) return loadingProportionChart()

    val singleGenders = slices.filter { it.gender != AuthorshipGender.Unknown && it.gender != AuthorshipGender.Mixed }

    return ProportionChartUiModel(
        segments = slices
            .map { slice ->
                ChartSliceUiModel(
                    label = slice.gender.toDisplayLabel(),
                    share = percentLabel(fraction = slice.fraction.toFloat()),
                    fraction = slice.fraction.toFloat(),
                    tone = when (slice.gender) {
                        AuthorshipGender.Unknown -> ChartTone.Unknown
                        AuthorshipGender.Mixed -> ChartTone.forRank(singleGenders.size)
                        else -> ChartTone.forRank(singleGenders.indexOf(slice))
                    },
                )
            }
            .toImmutableList(),
    )
}

internal fun authorChartUiModel(breakdown: DemographicBreakdown?): ProportionChartUiModel {
    if (breakdown == null) return loadingProportionChart()

    val total = breakdown.total.coerceAtLeast(1)

    val segments = listOfNotNull(
        shareSegment(
            label = "Yes",
            count = breakdown.yesCount,
            total = total,
            tone = ChartTone.Lead,
        ),
        shareSegment(
            label = "No",
            count = breakdown.noCount,
            total = total,
            tone = ChartTone.Fourth,
        ),
        shareSegment(
            label = "Unknown",
            count = breakdown.unknownCount,
            total = total,
            tone = ChartTone.Unknown,
        ).takeIf { breakdown.unknownCount > 0 },
    )

    return ProportionChartUiModel(segments = segments.toImmutableList())
}

internal fun characterChartUiModel(breakdown: CharacterBreakdown?): ProportionChartUiModel {
    if (breakdown == null) return loadingProportionChart()

    val total = breakdown.total.coerceAtLeast(1)

    val segments = listOf(
        shareSegment(
            label = "Yes",
            count = breakdown.yesCount,
            total = total,
            tone = ChartTone.Lead,
        ),
        shareSegment(
            label = "Complicated",
            count = breakdown.complicatedCount,
            total = total,
            tone = ChartTone.Third,
        ),
        shareSegment(
            label = "No",
            count = breakdown.noCount,
            total = total,
            tone = ChartTone.Fourth,
        ),
        shareSegment(
            label = "Not answered",
            count = breakdown.notAnsweredCount,
            total = total,
            tone = ChartTone.Unknown,
        ),
    ).filter { it.fraction > 0f }

    return ProportionChartUiModel(segments = segments.toImmutableList())
}

private fun diversityChapterHeader(hideBooksWithoutDetail: Boolean): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = atlasChapterEyebrow(AtlasChapter.DIVERSITY_AND_PUBLICATION),
    headline = "Whose stories",
    description = if (hideBooksWithoutDetail) {
        "Every line names its own total."
    } else {
        "Every line names its own total, and how many books it has no detail for."
    },
)

private fun loadingDiversityChapter(header: SectionHeaderUiModel.Section): DiversityChapterUiModel {
    fun loading(label: String) = DemographicBreakdownUiModel(
        label = label,
        chart = loadingProportionChart(),
        caption = "",
        hasData = false,
    )

    return DiversityChapterUiModel(
        header = header,
        genderBreakdown = loading(GENDER_LABEL),
        authorBipoc = loading(AUTHOR_BIPOC_LABEL),
        authorLgbtq = loading(AUTHOR_LGBTQ_LABEL),
        characterBipoc = loading(CHARACTER_BIPOC_LABEL),
        characterLgbtq = loading(CHARACTER_LGBTQ_LABEL),
        scopeCaption = "",
        characterSourceCaption = CHARACTER_SOURCE_CAPTION,
        emptyCaption = "",
        canHideBooksWithoutDetail = false,
    )
}

private fun AuthorDemographics.hasUnknownBooks(): Boolean = unknownGenderCount > 0 ||
    bipocBreakdown.unknownCount > 0 ||
    lgbtqBreakdown.unknownCount > 0

private fun CharacterRepresentation.hasUnansweredBooks(): Boolean = bipoc.notAnsweredCount > 0 ||
    lgbtq.notAnsweredCount > 0

private fun shareSegment(
    label: String,
    count: Int,
    total: Int,
    tone: ChartTone,
): ChartSliceUiModel {
    val fraction = count.toDouble() / total

    return ChartSliceUiModel(
        label = label,
        share = percentLabel(fraction = fraction.toFloat()),
        fraction = fraction.toFloat(),
        tone = tone,
    )
}

private fun loadingProportionChart(): ProportionChartUiModel = ProportionChartUiModel(
    segments = List(LOADING_SEGMENT_COUNT) { index ->
        ChartSliceUiModel(
            label = "",
            share = "",
            fraction = 0f,
            tone = ChartTone.forRank(index),
        )
    }.toImmutableList(),
    isLoading = true,
)

/**
 * `Other` stays the plain word: the bucket mixes non-binary and trans authors, and a narrower label
 * would misgender real people.
 */
private fun AuthorshipGender.toDisplayLabel(): String = when (this) {
    AuthorshipGender.Female -> "Women"
    AuthorshipGender.Male -> "Men"
    AuthorshipGender.Other -> "Other"
    AuthorshipGender.Mixed -> "Mixed"
    AuthorshipGender.Unknown -> "Unknown"
}

private fun genderCaption(
    bookCount: Int,
    unknownCount: Int,
    hideBooksWithoutDetail: Boolean,
): String {
    if (bookCount == 0) return "This fills in as your authors get gender data on Hardcover."

    if (hideBooksWithoutDetail) return "Across $bookCount books with the authors' gender."

    if (unknownCount > 0) return "Across $bookCount books, $unknownCount with no gender data."

    return "Across $bookCount books."
}

private fun authorCaption(
    breakdown: DemographicBreakdown,
    hideBooksWithoutDetail: Boolean,
): String {
    if (breakdown.total == 0) {
        if (hideBooksWithoutDetail) return "None of your books carry this detail yet."

        return "This fills in as your authors get tagged on Hardcover."
    }

    val counts = listOfNotNull(
        "Yes ${breakdown.yesCount}",
        "No ${breakdown.noCount}",
        "Unknown ${breakdown.unknownCount}".takeIf { breakdown.unknownCount > 0 },
    ).joinToString(separator = SEPARATOR)

    return "$counts$SEPARATOR${bookTotal(
        total = breakdown.total,
        hideBooksWithoutDetail = hideBooksWithoutDetail,
    )}"
}

private fun characterCaption(
    breakdown: CharacterBreakdown,
    hideBooksWithoutDetail: Boolean,
): String {
    if (breakdown.total == 0) {
        if (hideBooksWithoutDetail) return "None of your books have a How it read answer for this yet."

        return "This fills in as you answer How it read."
    }

    val counts = listOfNotNull(
        "Yes ${breakdown.yesCount}",
        "Complicated ${breakdown.complicatedCount}",
        "No ${breakdown.noCount}",
        "Not answered ${breakdown.notAnsweredCount}".takeIf { breakdown.notAnsweredCount > 0 },
    ).joinToString(separator = SEPARATOR)

    return "$counts$SEPARATOR${bookTotal(
        total = breakdown.total,
        hideBooksWithoutDetail = hideBooksWithoutDetail,
    )}"
}

private fun bookTotal(
    total: Int,
    hideBooksWithoutDetail: Boolean,
): String = if (hideBooksWithoutDetail) "of $total books with this detail" else "of $total books"
