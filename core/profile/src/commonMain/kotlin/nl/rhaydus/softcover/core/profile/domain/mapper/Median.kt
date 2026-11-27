package nl.rhaydus.softcover.core.profile.domain.mapper

internal fun List<Int>.medianOrNull(): Int? = if (isEmpty()) null else median()

/** An even count averages the middle two, rounded half up. */
internal fun List<Int>.median(): Int {
    val sorted = sorted()
    val middle = sorted.size / 2

    return if (sorted.size % 2 == 1) {
        sorted[middle]
    } else {
        (sorted[middle - 1] + sorted[middle] + 1) / 2
    }
}

/** An even count averages the middle two, rounded half up. */
internal fun List<Long>.median(): Long {
    val sorted = sorted()
    val middle = sorted.size / 2

    return if (sorted.size % 2 == 1) {
        sorted[middle]
    } else {
        (sorted[middle - 1] + sorted[middle] + 1) / 2
    }
}
