package nl.rhaydus.softcover.feature.profile.presentation.mapper

import nl.rhaydus.common.formatGroupedNumber

/**
 * The finished and stopped counts as phrases, each omitted at zero. The stopped phrase repeats the noun
 * only when no finished phrase precedes it.
 */
internal fun finishedAndStoppedBookParts(
    finished: Int,
    stopped: Int,
): List<String> = buildList {
    if (finished > 0) add("${formatGroupedNumber(finished)} finished ${bookNoun(finished)}")

    if (stopped > 0) {
        val count = formatGroupedNumber(stopped)

        add(if (finished > 0) "$count you didn't finish" else "$count ${bookNoun(stopped)} you didn't finish")
    }
}

private fun bookNoun(count: Int): String = if (count == 1) "book" else "books"
