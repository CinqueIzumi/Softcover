package nl.rhaydus.softcover.core.profile.domain.model

enum class LengthBand {
    UNDER_250,
    FROM_250_TO_450,
    OVER_450,
    ;

    companion object {
        /** Null when the length is unknown or not positive. */
        fun fromPages(pages: Int?): LengthBand? = when {
            pages == null || pages <= 0 -> null
            pages < 250 -> UNDER_250
            pages <= 450 -> FROM_250_TO_450
            else -> OVER_450
        }
    }
}
