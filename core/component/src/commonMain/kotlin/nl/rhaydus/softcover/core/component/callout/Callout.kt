package nl.rhaydus.softcover.core.component.callout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.header.SectionHeaderEyebrowText
import nl.rhaydus.softcover.core.component.header.SectionHeaderInlineEyebrow
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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
        shape = Corners.lg,
    ) {
        Row(
            modifier = Modifier.padding(
                start = Spacing.lg,
                top = Spacing.md,
                end = if (model.dismissible) Spacing.xxs else Spacing.lg,
                bottom = Spacing.md,
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

    val motionScheme = MaterialTheme.motionScheme

    val header = CalloutHeader(
        eyebrow = model.eyebrow,
        variant = model.variant,
        body = model.body,
    )

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = header,
            contentKey = { it.key },
            transitionSpec = {
                fadeIn(animationSpec = motionScheme.defaultEffectsSpec())
                    .togetherWith(fadeOut(animationSpec = motionScheme.defaultEffectsSpec()))
                    .using(SizeTransform { _, _ -> motionScheme.defaultSpatialSpec() })
            },
        ) { target ->
            CalloutHeaderContent(
                header = target,
                content = content,
            )
        }

        AnimatedContent(
            targetState = model.callToAction,
            contentKey = { it?.stageKey() },
            transitionSpec = {
                fadeIn(animationSpec = motionScheme.defaultEffectsSpec())
                    .togetherWith(fadeOut(animationSpec = motionScheme.defaultEffectsSpec()))
                    .using(SizeTransform { _, _ -> motionScheme.defaultSpatialSpec() })
            },
        ) { action ->
            if (action != null) {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    CalloutActionContent(
                        action = action,
                        variant = model.variant,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

private data class CalloutHeader(
    val eyebrow: String,
    val variant: CalloutVariant,
    val body: String,
) {
    val key: ImmutableList<String?> = persistentListOf(
        eyebrow,
        (variant as? CalloutVariant.Highlight)?.title,
        body,
    )
}

private fun CalloutActionUiModel.stageKey(): Any = when (this) {
    is CalloutActionUiModel.Progress -> CalloutActionUiModel.Progress::class
    is CalloutActionUiModel.Button,
    is CalloutActionUiModel.Busy,
    -> this
}

@Composable
private fun CalloutHeaderContent(
    header: CalloutHeader,
    content: Color,
) {
    Column {
        CalloutEyebrow(
            eyebrow = header.eyebrow,
            variant = header.variant,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        if (header.variant is CalloutVariant.Highlight) {
            SoftcoverText(
                text = header.variant.title,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = content,
            )

            Spacer(modifier = Modifier.height(Spacing.xs))
        }

        SoftcoverText(
            text = header.body,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = content,
        )
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

                Spacer(modifier = Modifier.width(Spacing.xs))
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

            Spacer(modifier = Modifier.width(Spacing.sm))

            SoftcoverText(
                text = action.label,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = mutedInk(variant),
            )
        }

        is CalloutActionUiModel.Progress -> CalloutProgressBar(fraction = action.fraction)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CalloutProgressBar(fraction: Float?) {
    if (fraction == null) {
        LinearWavyProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        val animated by animateFloatAsState(
            targetValue = fraction,
            animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        )

        LinearWavyProgressIndicator(
            progress = { animated },
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
