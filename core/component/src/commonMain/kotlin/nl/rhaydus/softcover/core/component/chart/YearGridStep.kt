package nl.rhaydus.softcover.core.component.chart

/** How many pages a read day holds, from the lightest tint to the darkest. */
enum class YearGridStep {
    One,
    Two,
    Three,
    Four,
}

internal fun YearGridStep.tone(): ChartTone = when (this) {
    YearGridStep.One -> ChartTone.Fifth
    YearGridStep.Two -> ChartTone.Third
    YearGridStep.Three -> ChartTone.Second
    YearGridStep.Four -> ChartTone.Lead
}
