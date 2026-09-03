package nl.rhaydus.softcover.core.component.share

import kotlinx.collections.immutable.ImmutableList

data class YearRecapShareCardUiModel(
    val year: Int,
    val eyebrow: String,
    val headline: String,
    val highlights: ImmutableList<String>,
) : ShareCardUiModel
