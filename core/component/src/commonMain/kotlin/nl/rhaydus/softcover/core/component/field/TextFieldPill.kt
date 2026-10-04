package nl.rhaydus.softcover.core.component.field

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.text.SoftcoverText
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
    val dimensions = variant.size.dimensions()

    // The exit animation needs the pill after the model has already dropped it.
    var retainedTrailing by remember { mutableStateOf(variant.trailing) }

    LaunchedEffect(variant.trailing) {
        if (variant.trailing != null) retainedTrailing = variant.trailing
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.height)
            .clip(RoundedCornerShape(percent = 50))
            .background(dimensions.container())
            .padding(
                start = dimensions.horizontalPadding,
                end = if (variant.trailing == null) dimensions.horizontalPadding else 8.dp,
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensions.iconSize),
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (model.text.isEmpty()) {
                SoftcoverText(
                    text = model.placeholder,
                    style = dimensions.textStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dimensions.placeholderAlpha),
                )
            }

            BasicTextField(
                value = model.text,
                onValueChange = { text ->
                    onEvent(
                        TextFieldEvent.ValueChanged(
                            key = model.key,
                            text = text,
                        ),
                    )
                },
                enabled = model.enabled,
                singleLine = true,
                textStyle = dimensions.textStyle().copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = model.keyboardOptions(),
                keyboardActions = KeyboardActions(onDone = { onEvent(TextFieldEvent.Submitted(key = model.key)) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
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
}
