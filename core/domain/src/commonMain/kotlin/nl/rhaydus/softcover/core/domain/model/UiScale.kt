package nl.rhaydus.softcover.core.domain.model

enum class UiScale(
    val factor: Float,
    val label: String,
) {
    PERCENT_100(factor = 1.0f, label = "100%"),
    PERCENT_125(factor = 1.25f, label = "125%"),
    PERCENT_150(factor = 1.5f, label = "150%"),
    PERCENT_175(factor = 1.75f, label = "175%"),
    PERCENT_200(factor = 2.0f, label = "200%"),
    ;

    companion object {
        val DEFAULT: UiScale = PERCENT_100
    }
}
