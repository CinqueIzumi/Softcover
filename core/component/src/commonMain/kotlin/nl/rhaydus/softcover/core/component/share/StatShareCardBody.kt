package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun StatShareCardBody(content: StatShareCardUiModel) {
    SoftcoverText(
        text = content.eyebrow.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrow,
        color = LocalContentColor.current.copy(alpha = 0.75f),
    )

    Spacer(modifier = Modifier.height(Spacing.xl))

    SoftcoverText(
        text = content.value.toString(),
        style = MaterialTheme.editorialTypography.statHero.copy(fontFeatureSettings = "tnum"),
        color = LocalContentColor.current,
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    SoftcoverText(
        text = content.caption,
        style = MaterialTheme.editorialTypography.body,
        color = LocalContentColor.current.copy(alpha = 0.75f),
    )
}
