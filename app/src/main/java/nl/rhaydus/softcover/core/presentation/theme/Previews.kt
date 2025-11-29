package nl.rhaydus.softcover.core.presentation.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "System default/Dark",
    showBackground = true,
    group = "Default light/dark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "System default/Light",
    showBackground = true,
    group = "Default light/dark"
)
annotation class StandardPreview