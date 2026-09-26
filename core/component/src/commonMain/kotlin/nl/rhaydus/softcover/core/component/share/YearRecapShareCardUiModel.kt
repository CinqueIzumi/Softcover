package nl.rhaydus.softcover.core.component.share

import kotlinx.collections.immutable.ImmutableList

/** A year-in-review recap: [eyebrow] + [year], a display [headline], and a bulleted list of [highlights]. */
data class YearRecapShareCardUiModel(
    val year: Int,
    val eyebrow: String,
    val headline: String,
    val highlights: ImmutableList<String>,
) : ShareCardUiModel
