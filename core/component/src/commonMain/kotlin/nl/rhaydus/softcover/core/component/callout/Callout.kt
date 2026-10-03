package nl.rhaydus.softcover.core.component.callout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.header.SectionHeaderEyebrowText
import nl.rhaydus.softcover.core.component.header.SectionHeaderInlineEyebrow
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
fun Callout(
    model: CalloutUiModel,
    onEvent: (CalloutEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val style = calloutStyle(
        tone = model.tone,
        scheme = MaterialTheme.colorScheme,
    )

    Surface(
        modifier = modifier,
        color = style.container,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                top = 18.dp,
                end = if (model.dismissible) 4.dp else 20.dp,
                bottom = 18.dp,
            ),
            verticalAlignment = Alignment.Top,
        ) {
            CalloutContent(
                model = model,
                style = style,
                onEvent = onEvent,
                modifier = Modifier.weight(1f),
            )

            if (model.dismissible) {
                ChromeIconButton(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Dismiss",
                    tooltip = "Dismiss",
                    onClick = { onEvent(CalloutEvent.DismissClicked) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = style.mutedInk),
                    iconSize = 16.dp,
                )
            }
        }
    }
}

@Composable
private fun CalloutContent(
    model: CalloutUiModel,
    style: CalloutStyle,
    onEvent: (CalloutEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CalloutEyebrow(
            eyebrow = model.eyebrow,
            lead = model.lead,
            style = style,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (model.title != null) {
            SoftcoverText(
                text = model.title,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = style.content,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        SoftcoverText(
            text = model.body,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = style.content,
        )

        if (model.action != null) {
            Spacer(modifier = Modifier.height(12.dp))

            CalloutActionContent(
                action = model.action,
                style = style,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun CalloutEyebrow(
    eyebrow: String,
    lead: CalloutLead?,
    style: CalloutStyle,
) {
    when (lead) {
        CalloutLead.AccentBar -> SectionHeaderInlineEyebrow(
            eyebrow = eyebrow,
            color = style.accent,
        )

        null, is CalloutLead.Icon -> Row(verticalAlignment = Alignment.CenterVertically) {
            if (lead is CalloutLead.Icon) {
                val resource = drawableIconResource(
                    icon = lead.icon,
                    contentDescription = "",
                )

                Icon(
                    painter = resource.getIconPainter(),
                    contentDescription = null,
                    tint = style.accent,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            SectionHeaderEyebrowText(
                eyebrow = eyebrow,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = style.accent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CalloutActionContent(
    action: CalloutActionUiModel,
    style: CalloutStyle,
    onEvent: (CalloutEvent) -> Unit,
) {
    when (action) {
        is CalloutActionUiModel.Button -> RhaydusButton(
            label = action.label,
            style = style.buttonStyle,
            size = ButtonSize.S,
            onClick = { onEvent(CalloutEvent.ActionClicked) },
        )

        is CalloutActionUiModel.Busy -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))

            Spacer(modifier = Modifier.width(10.dp))

            SoftcoverText(
                text = action.label,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = style.mutedInk,
            )
        }

        CalloutActionUiModel.Progress -> LinearWavyProgressIndicator(
            color = style.accent,
            trackColor = style.track,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
