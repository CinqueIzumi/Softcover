package nl.rhaydus.softcover.core.component.field

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PillTextField(
    model: TextFieldUiModel,
    variant: TextFieldVariant.Pill,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val height = when (variant.size) {
        TextFieldPillSize.Compact -> 42.dp
        TextFieldPillSize.Regular -> 48.dp
    }
    val horizontalPadding = 14.dp
    val iconSize = if (variant.size == TextFieldPillSize.Compact) 18.dp else 20.dp
    val textStyle = when (variant.size) {
        TextFieldPillSize.Compact -> MaterialTheme.typography.bodyMedium
        TextFieldPillSize.Regular -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
    }
    val container = when (variant.size) {
        TextFieldPillSize.Compact -> MaterialTheme.colorScheme.surfaceContainer
        TextFieldPillSize.Regular -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    // The exit animation needs the pill after the model has already dropped it.
    var retainedTrailing by remember { mutableStateOf(variant.trailing) }

    LaunchedEffect(variant.trailing) {
        if (variant.trailing != null) retainedTrailing = variant.trailing
    }

    // Eased alongside the pill's own fade so the field's edge does not jump when the pill leaves.
    val endPadding: Dp by animateDpAsState(
        targetValue = if (variant.trailing == null) horizontalPadding else 8.dp,
        animationSpec = if (playMotion) MaterialTheme.motionScheme.fastSpatialSpec() else snap(),
    )

    TextFieldInput(
        model = model,
        textStyle = textStyle,
        onEvent = onEvent,
        focusRequester = focusRequester,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(container),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(
                        start = horizontalPadding,
                        end = endPadding,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                variant.leadingIcon?.let { icon ->
                    val leading = drawableIconResource(
                        contentDescription = "",
                        icon = icon,
                    )

                    Icon(
                        painter = leading.getIconPainter(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.forEnabled(model.enabled),
                        modifier = Modifier.size(iconSize),
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (model.text.isEmpty()) {
                        TextFieldPlaceholder(
                            text = model.placeholder,
                            enabled = model.enabled,
                            style = textStyle,
                        )
                    }

                    innerTextField()
                }

                AnimatedVisibility(
                    visible = variant.trailing != null,
                    enter = if (playMotion) {
                        fadeIn(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()) +
                            scaleIn(
                                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                initialScale = 0.9f,
                            )
                    } else {
                        EnterTransition.None
                    },
                    exit = if (playMotion) {
                        fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()) +
                            scaleOut(
                                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                targetScale = 0.9f,
                            )
                    } else {
                        ExitTransition.None
                    },
                ) {
                    (variant.trailing ?: retainedTrailing)?.let { trailing ->
                        PillButton(
                            model = trailing,
                            onEvent = { onEvent(TextFieldEvent.TrailingClicked(key = model.key)) },
                        )
                    }
                }
            }
        },
    )
}
