package nl.rhaydus.softcover.core.designsystem.presentation.util

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberClipboardReader(): ClipboardReader {
    val context = LocalContext.current

    return remember(context) {
        ClipboardReader {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = clipboard?.primaryClip

            if (clip == null || clip.itemCount == 0) {
                ""
            } else {
                clip.getItemAt(0).coerceToText(context).toString()
            }
        }
    }
}
