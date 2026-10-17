package nl.rhaydus.softcover.feature.widget.domain.util

internal fun resolveShownBookId(
    storedBookId: Int?,
    orderedBookIds: List<Int>,
): Int? = storedBookId?.takeIf { it in orderedBookIds } ?: orderedBookIds.firstOrNull()

/** The book [step] places from the shown one, wrapping around the shelf. */
internal fun nextBookId(
    storedBookId: Int?,
    orderedBookIds: List<Int>,
    step: Int,
): Int? {
    val shownId = resolveShownBookId(
        storedBookId = storedBookId,
        orderedBookIds = orderedBookIds,
    ) ?: return null

    val nextIndex = (orderedBookIds.indexOf(shownId) + step).mod(orderedBookIds.size)

    return orderedBookIds[nextIndex]
}
