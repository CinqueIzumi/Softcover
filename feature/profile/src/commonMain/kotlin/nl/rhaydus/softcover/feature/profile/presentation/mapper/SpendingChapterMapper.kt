package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartSliceUiModel
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChartVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.profile.domain.model.CurrencySpend
import nl.rhaydus.softcover.core.profile.domain.model.SourceSpend
import nl.rhaydus.softcover.core.profile.domain.model.SpendSource
import nl.rhaydus.softcover.core.profile.domain.model.SpendStats
import nl.rhaydus.softcover.core.uibinding.acquisition.toLabel
import nl.rhaydus.softcover.core.uibinding.money.formatWholeUnits
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.SpendBlockUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.SpendingChapterUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.VersusCellUiModel

private const val HEADLINE = "What you paid"
private const val EMPTY_LINE = "Note a price under Your copy and what you paid shows here."
private const val SPENT_EYEBROW = "Spent"
private const val TYPICAL_EYEBROW = "Typical price"
private const val TYPICAL_CAPTION = "a book, the median."
private const val SOMEWHERE_ELSE_LABEL = "Somewhere else"
private const val NOT_NOTED_LABEL = "Not noted"
private const val THOUSAND = 1000L
private const val HALF_THOUSAND = 500L
private const val LOADING_SEGMENT_COUNT = 3
private const val STAND_IN_PRICED_BOOKS = 18
private const val STAND_IN_CURRENCY = "EUR"

/**
 * A null [stats] yields the masked stand-in. The lead currency is [usual] when a book in scope has a price
 * in it, else the currency with the most priced books; a currency missing from [currencies] shows its code.
 */
internal fun spendingChapterUiModel(
    stats: SpendStats?,
    currencies: List<Currency>,
    usual: Currency?,
): SpendingChapterUiModel {
    if (stats == null) return loadingSpendingChapter()

    if (stats.currencies.isEmpty()) {
        return SpendingChapterUiModel(
            header = spendingHeader(description = null),
            emptyLine = EMPTY_LINE,
            spent = null,
            typical = null,
            blocks = persistentListOf(),
        )
    }

    val lead = stats.currencies.leadCurrency(usual?.code)

    val others = (stats.currencies - lead)
        .sortedWith(compareByDescending<CurrencySpend> { it.pricedBooks }.thenBy { it.currencyCode })

    val leadCurrency = currencyFor(
        lead.currencyCode,
        currencies,
    )

    val otherCurrencies = others.map {
        it to currencyFor(
            it.currencyCode,
            currencies,
        )
    }

    return SpendingChapterUiModel(
        header = spendingHeader(description = description(stats.pricedBooks)),
        emptyLine = null,
        spent = VersusCellUiModel(
            eyebrow = SPENT_EYEBROW,
            stat = StatNumberUiModel(
                value = lead.totalThousandths.wholeUnits().toDouble(),
                prefix = leadCurrency.symbol,
            ),
            caption = spentCaption(
                leadCode = lead.currencyCode,
                others = otherCurrencies,
            ),
            isLead = true,
        ),
        typical = VersusCellUiModel(
            eyebrow = TYPICAL_EYEBROW,
            stat = StatNumberUiModel(
                value = lead.medianThousandths.wholeUnits().toDouble(),
                prefix = leadCurrency.symbol,
            ),
            caption = TYPICAL_CAPTION,
            isLead = false,
        ),
        blocks = buildList {
            add(
                SpendBlockUiModel(
                    title = "Where it went, in ${lead.currencyCode}",
                    chart = lead.chart(
                        currency = leadCurrency,
                        variant = ProportionChartVariant.Rows,
                    ),
                ),
            )

            otherCurrencies.forEach { (spend, currency) ->
                add(
                    SpendBlockUiModel(
                        title = "In ${spend.currencyCode}",
                        chart = spend.chart(
                            currency = currency,
                            variant = ProportionChartVariant.Legend,
                        ),
                    ),
                )
            }
        }.toImmutableList(),
    )
}

private fun List<CurrencySpend>.leadCurrency(usualCode: String?): CurrencySpend =
    firstOrNull { it.currencyCode == usualCode }
        ?: sortedWith(compareByDescending<CurrencySpend> { it.pricedBooks }.thenBy { it.currencyCode }).first()

private fun currencyFor(
    code: String,
    currencies: List<Currency>,
): Currency = currencies.firstOrNull { it.code == code }
    ?: Currency(
        code = code,
        name = code,
        symbol = code,
        fractionDigits = 0,
    )

private fun spendingHeader(description: String?): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = atlasChapterEyebrow(AtlasChapter.RECOMMENDERS_AND_SPENDING),
    headline = HEADLINE,
    description = description,
)

private fun description(pricedBooks: Int): String =
    "Across the $pricedBooks ${plural(pricedBooks)} with a price. Each currency keeps its own total."

private fun plural(count: Int): String = if (count == 1) "book" else "books"

private fun Long.wholeUnits(): Long = (this + HALF_THOUSAND) / THOUSAND

private fun spentCaption(
    leadCode: String,
    others: List<Pair<CurrencySpend, Currency>>,
): String {
    if (others.isEmpty()) return "All in $leadCode."

    val parts = others.map { (spend, currency) ->
        "${formatWholeUnits(
            spend.totalThousandths,
            currency,
        )} on ${spend.pricedBooks} ${plural(spend.pricedBooks)}"
    }

    val joined = if (parts.size == 1) {
        parts.single()
    } else {
        "${parts.dropLast(1).joinToString(", ")} and ${parts.last()}"
    }

    return "plus $joined."
}

private fun CurrencySpend.chart(
    currency: Currency,
    variant: ProportionChartVariant,
): ProportionChartUiModel {
    var presetRank = 0

    return ProportionChartUiModel(
        segments = sources.map { source ->
            val tone = when (source.source) {
                is SpendSource.Preset -> ChartTone.forRank(presetRank++)
                SpendSource.SomewhereElse -> ChartTone.Fifth
                SpendSource.NotNoted -> ChartTone.Unknown
            }

            ChartSliceUiModel(
                label = source.source.label(),
                share = "${formatWholeUnits(
                    source.totalThousandths,
                    currency,
                )} · ${source.books} ${plural(source.books)}",
                fraction = source.fractionOf(totalThousandths),
                tone = tone,
            )
        }.toImmutableList(),
        variant = variant,
    )
}

private fun SourceSpend.fractionOf(total: Long): Float =
    if (total <= 0L) 0f else totalThousandths.toFloat() / total

private fun SpendSource.label(): String = when (this) {
    is SpendSource.Preset -> kind.toLabel()
    SpendSource.SomewhereElse -> SOMEWHERE_ELSE_LABEL
    SpendSource.NotNoted -> NOT_NOTED_LABEL
}

private fun loadingSpendingChapter(): SpendingChapterUiModel = SpendingChapterUiModel(
    header = spendingHeader(description = description(STAND_IN_PRICED_BOOKS)).copy(isLoading = true),
    emptyLine = null,
    spent = loadingCell(
        eyebrow = SPENT_EYEBROW,
        caption = "All in $STAND_IN_CURRENCY.",
        isLead = true,
    ),
    typical = loadingCell(
        eyebrow = TYPICAL_EYEBROW,
        caption = TYPICAL_CAPTION,
        isLead = false,
    ),
    blocks = persistentListOf(
        SpendBlockUiModel(
            title = "Where it went, in $STAND_IN_CURRENCY",
            chart = ProportionChartUiModel(
                segments = List(LOADING_SEGMENT_COUNT) { index ->
                    ChartSliceUiModel(
                        label = "",
                        share = "",
                        fraction = 0f,
                        tone = ChartTone.forRank(index),
                    )
                }.toImmutableList(),
                isLoading = true,
                variant = ProportionChartVariant.Rows,
            ),
        ),
    ),
)

private fun loadingCell(
    eyebrow: String,
    caption: String,
    isLead: Boolean,
) = VersusCellUiModel(
    eyebrow = eyebrow,
    stat = StatNumberUiModel(
        value = 0.0,
        format = StatNumberFormat.Grouped,
        isLoading = true,
    ),
    caption = caption,
    isLead = isLead,
)
