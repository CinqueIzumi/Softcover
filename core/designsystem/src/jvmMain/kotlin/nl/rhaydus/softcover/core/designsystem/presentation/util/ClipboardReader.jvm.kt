package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

@Composable
actual fun rememberClipboardReader(): ClipboardReader =
    remember {
        ClipboardReader {
            runCatching {
                Toolkit.getDefaultToolkit().systemClipboard
                    .getData(DataFlavor.stringFlavor) as? String
            }.getOrNull().orEmpty()
        }
    }
