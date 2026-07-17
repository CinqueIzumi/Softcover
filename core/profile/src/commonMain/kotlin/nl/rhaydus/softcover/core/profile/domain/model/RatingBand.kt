package nl.rhaydus.softcover.core.profile.domain.model

enum class RatingBand {
    GENEROUS,
    SUPERFAN,
    CENTRIST,
    EXACTING,
    POLARISED,
    NEW_READER,
    ;

    companion object {
        private const val NEW_READER_MIN_RATINGS = 15
        private const val SUPERFAN_AVERAGE_THRESHOLD = 4.6
        private const val EXACTING_AVERAGE_THRESHOLD = 3.2
        private const val CENTRIST_AVERAGE_MIN = 3.3
        private const val CENTRIST_AVERAGE_MAX = 3.8
        private const val BIMODAL_WING_FRACTION = 0.2
        private const val WING_BUCKET_COUNT = 3
        private const val MID_BUCKET_COUNT = 3

        // Reads as generous by default; the sharper bands (superfan, exacting, centrist, polarised)
        // each require their own distinct signal in the distribution to override it. New reader takes
        // priority over every shape-based band, since too few ratings makes any shape unreliable.
        fun classify(
            halfStarBuckets: List<Int>,
            average: Double,
            totalRatings: Int,
        ): RatingBand = when {
            totalRatings < NEW_READER_MIN_RATINGS -> NEW_READER
            isBimodal(
                halfStarBuckets,
                totalRatings,
            ) -> POLARISED
            average >= SUPERFAN_AVERAGE_THRESHOLD -> SUPERFAN
            average < EXACTING_AVERAGE_THRESHOLD -> EXACTING
            average in CENTRIST_AVERAGE_MIN..CENTRIST_AVERAGE_MAX -> CENTRIST
            else -> GENEROUS
        }

        // A distribution is bimodal when both the low (1.0-2.0★) and high (4.0-5.0★) wings each carry
        // a meaningful share of all ratings, with the middle band (2.5-3.5★) a genuine trough between
        // them rather than the bulk of the mass.
        private fun isBimodal(
            halfStarBuckets: List<Int>,
            totalRatings: Int,
        ): Boolean {
            if (totalRatings == 0) return false

            val low = halfStarBuckets.take(WING_BUCKET_COUNT).sum()
            val mid = halfStarBuckets
                .drop(WING_BUCKET_COUNT)
                .take(MID_BUCKET_COUNT)
                .sum()
            val high = halfStarBuckets.takeLast(WING_BUCKET_COUNT).sum()

            val lowFraction = low.toDouble() / totalRatings
            val midFraction = mid.toDouble() / totalRatings
            val highFraction = high.toDouble() / totalRatings

            return lowFraction >= BIMODAL_WING_FRACTION &&
                highFraction >= BIMODAL_WING_FRACTION &&
                midFraction < minOf(
                    lowFraction,
                    highFraction,
                )
        }
    }
}
