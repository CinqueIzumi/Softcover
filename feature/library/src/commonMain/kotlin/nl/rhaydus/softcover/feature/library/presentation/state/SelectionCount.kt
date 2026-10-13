package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberMotion
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant

internal fun selectionCountOf(count: Int): StatNumberUiModel = StatNumberUiModel(
    value = count.toDouble(),
    format = StatNumberFormat.Plain,
    variant = StatNumberVariant.Headline,
    suffix = StatNumberSuffix.Fixed(" selected"),
    motion = StatNumberMotion.Roll,
)
