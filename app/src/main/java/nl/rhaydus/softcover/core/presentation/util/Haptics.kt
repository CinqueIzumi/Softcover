package nl.rhaydus.softcover.core.presentation.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

interface Haptics {
    fun commit()

    fun reject()
}

private class ViewHaptics(private val view: View) : Haptics {

    override fun commit() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    override fun reject() {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}

private object NoOpHaptics : Haptics {

    override fun commit() = Unit

    override fun reject() = Unit
}

val LocalHaptics = staticCompositionLocalOf<Haptics> { NoOpHaptics }

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current

    return remember(view) { ViewHaptics(view) }
}
