package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.Composable

/**
 * Reads the current plain-text contents of the system clipboard, returning an empty string when the
 * clipboard is empty or holds something that can't be coerced to text.
 */
fun interface ClipboardReader {
    fun read(): String
}

/**
 * The active [ClipboardReader] for the current platform — Android reads the primary clip from
 * `ClipboardManager`, iOS reads `UIPasteboard.generalPasteboard`. Exposed as a seam (rather than the raw
 * platform clipboard) so common UI can paste without touching `nativeClipboard`.
 */
@Composable
expect fun rememberClipboardReader(): ClipboardReader
