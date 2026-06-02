package nl.rhaydus.softcover.core.designsystem.presentation.share

data class YearRecapShareContent(
    val year: Int,
    val eyebrow: String,
    val headline: String,
    val highlights: List<String>,
) : ShareContent
