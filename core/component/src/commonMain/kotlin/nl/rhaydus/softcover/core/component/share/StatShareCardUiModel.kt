package nl.rhaydus.softcover.core.component.share

/** A single `primary`-filled hero numeral share — [eyebrow] over an oversize [value], with [caption] beneath. */
data class StatShareCardUiModel(
    val eyebrow: String,
    val value: Long,
    val caption: String,
) : ShareCardUiModel
