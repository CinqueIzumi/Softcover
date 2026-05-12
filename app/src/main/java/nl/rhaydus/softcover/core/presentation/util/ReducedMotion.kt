package nl.rhaydus.softcover.core.presentation.util

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Whether decorative motion should play on the current device.
 *
 * Reads `Settings.Global.ANIMATOR_DURATION_SCALE`: a scale of `0f` means the user has
 * disabled animations system-wide, so decorative motion must be skipped. Any positive
 * scale plays motion at its default duration — no graduated scaling.
 *
 * Functional motion (a state change the user just triggered — e.g. a list-item swipe or
 * a navigation push) still plays; this gate covers decorative motion only.
 */
@Composable
fun playDecorativeMotion(): Boolean {
    val context = LocalContext.current

    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )

        scale != 0f
    }
}
