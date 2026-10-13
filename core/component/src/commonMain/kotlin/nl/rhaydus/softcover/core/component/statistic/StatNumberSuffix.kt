package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable

@Immutable
sealed interface StatNumberSuffix {
    data class Fixed(val text: String) : StatNumberSuffix

    /** [one] while the integer on screen is 1, mid-spring included, otherwise [other]. */
    data class Plural(
        val one: String,
        val other: String,
    ) : StatNumberSuffix
}
