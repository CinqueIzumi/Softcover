package nl.rhaydus.softcover.core.component.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import nl.rhaydus.designsystem.haptics.Haptics
import nl.rhaydus.designsystem.haptics.LocalHaptics

private const val NO_INDEX = -1

class ReorderHaptics internal constructor(private val haptics: Haptics) {
    private var lastIndex = NO_INDEX

    /** Pass [fromIndex] when the pickup position is reported through [moved] too, so it does not tick. */
    fun lift(fromIndex: Int = NO_INDEX) {
        lastIndex = fromIndex

        haptics.lift()
    }

    fun moved(toIndex: Int) {
        if (toIndex == lastIndex) return

        lastIndex = toIndex

        haptics.tickle()
    }

    fun drop() {
        lastIndex = NO_INDEX

        haptics.drop()
    }
}

@Composable
fun rememberReorderHaptics(): ReorderHaptics {
    val haptics = LocalHaptics.current

    return remember(haptics) { ReorderHaptics(haptics) }
}
