package nl.rhaydus.softcover.core.domain.app

/**
 * Compares dotted version strings (`3.0.2`, `v3.0.2`) numerically, tolerant of a leading `v`, a
 * pre-release suffix (`-rc1`), build metadata (`+build`), and differing segment counts. Non-numeric
 * segments are treated as `0`. Pre-release/build parts are ignored because the updater only ever
 * compares against GitHub's stable `releases/latest`.
 */
object VersionComparator {
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

    /** `true` when [a] and [b] name the same release once parsed, e.g. `v3.2` and `3.2.0-rc1`. */
    fun isSameRelease(
        a: String,
        b: String,
    ): Boolean {
        val aParts = parse(a)
        val bParts = parse(b)
        val segments = maxOf(
            aParts.size,
            bParts.size,
        )

        return (0 until segments).all { aParts.getOrElse(it) { 0 } == bParts.getOrElse(it) { 0 } }
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
