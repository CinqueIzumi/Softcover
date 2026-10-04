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
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.header.SectionHeaderEyebrowText
import nl.rhaydus.softcover.core.component.header.SectionHeaderInlineEyebrow
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
fun Callout(
    model: CalloutUiModel,
    onEvent: (CalloutEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = if (model.variant is CalloutVariant.Highlight) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
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
                onEvent = onEvent,
                modifier = Modifier.weight(1f),
            )

            if (model.dismissible) {
                DismissButton(
                    contentColor = mutedInk(model.variant),
                    onClick = { onEvent(CalloutEvent.DismissClicked) },
                )
            }
        }
    }
}

@Composable
private fun CalloutContent(
    model: CalloutUiModel,
    onEvent: (CalloutEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = if (model.variant is CalloutVariant.Highlight) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = modifier) {
        CalloutEyebrow(
            eyebrow = model.eyebrow,
            variant = model.variant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (model.variant is CalloutVariant.Highlight) {
            SoftcoverText(
                text = model.variant.title,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = content,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        SoftcoverText(
            text = model.body,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = content,
        )

        if (model.callToAction != null) {
            Spacer(modifier = Modifier.height(12.dp))

            CalloutActionContent(
                action = model.callToAction,
                variant = model.variant,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun CalloutEyebrow(
    eyebrow: String,
    variant: CalloutVariant,
) {
    val accent = MaterialTheme.colorScheme.primary

    when (variant) {
        is CalloutVariant.Highlight -> SectionHeaderInlineEyebrow(
            eyebrow = eyebrow,
            color = accent,
        )

        is CalloutVariant.Neutral -> Row(verticalAlignment = Alignment.CenterVertically) {
            if (variant.icon != null) {
                val resource = drawableIconResource(
                    icon = variant.icon,
                    contentDescription = "",
                )

                Icon(
                    painter = resource.getIconPainter(),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            SectionHeaderEyebrowText(
                eyebrow = eyebrow,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = accent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CalloutActionContent(
    action: CalloutActionUiModel,
    variant: CalloutVariant,
    onEvent: (CalloutEvent) -> Unit,
) {
    when (action) {
        is CalloutActionUiModel.Button -> RhaydusButton(
            label = action.label,
            style = if (variant is CalloutVariant.Highlight) ButtonStyle.FILLED else ButtonStyle.TONAL,
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
                color = mutedInk(variant),
            )
        }

        CalloutActionUiModel.Progress -> LinearWavyProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun mutedInk(variant: CalloutVariant): Color = if (variant is CalloutVariant.Highlight) {
    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
} else {
    MaterialTheme.colorScheme.onSurfaceVariant
}
