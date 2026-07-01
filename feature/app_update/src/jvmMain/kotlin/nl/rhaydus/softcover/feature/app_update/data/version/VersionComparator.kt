package nl.rhaydus.softcover.feature.app_update.data.version

/**
 * Compares dotted version strings (`3.0.2`, `v3.0.2`) numerically, tolerant of a leading `v`, a
 * pre-release suffix (`-rc1`), build metadata (`+build`), and differing segment counts. Non-numeric
 * segments are treated as `0`. Pre-release/build parts are ignored because the updater only ever
 * compares against GitHub's stable `releases/latest`.
 */
internal object VersionComparator {
    /** `true` when [candidate] is a strictly higher version than [current]. */
    fun isNewer(
        candidate: String,
        current: String,
    ): Boolean {
        val candidateParts = parse(candidate)
        val currentParts = parse(current)
        val segments = maxOf(
            candidateParts.size,
            currentParts.size,
        )

        for (index in 0 until segments) {
            val candidateSegment = candidateParts.getOrElse(index) { 0 }
            val currentSegment = currentParts.getOrElse(index) { 0 }

            if (candidateSegment != currentSegment) return candidateSegment > currentSegment
        }

        return false
    }

    private fun parse(version: String): List<Int> {
        return version.trim()
            .trimStart(
                'v',
                'V',
            )
            .substringBefore('-')
            .substringBefore('+')
            .split('.')
            .map { it.trim().toIntOrNull() ?: 0 }
    }
}
