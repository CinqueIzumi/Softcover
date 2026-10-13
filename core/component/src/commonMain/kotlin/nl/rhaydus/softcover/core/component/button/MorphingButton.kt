package nl.rhaydus.softcover.core.component.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.softcover.core.component.modifier.tactilePressScale

/**
 * A [nl.rhaydus.designsystem.component.RhaydusButton] that animates when [tone] changes: container and
 * content colour, a crossfade of [label] and [icon], and the button's width. The press scale applies to
 * [MorphingButtonTone.Filled] alone, as in `RhaydusButton`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingButton(
    label: String,
    tone: MorphingButtonTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: RhaydusIconResource? = null,
) {
    val size = ButtonSize.S
    val motionScheme = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isFilled = tone == MorphingButtonTone.Filled
    val target = if (isFilled) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }
    val containerColor by animateColorAsState(
        targetValue = target.containerColor,
        animationSpec = motionScheme.defaultEffectsSpec(),
    )
    val contentColor by animateColorAsState(
        targetValue = target.contentColor,
        animationSpec = motionScheme.defaultEffectsSpec(),
    )

    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapesFor(buttonHeight = size.height),
        enabled = enabled,
        colors = target.copy(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = if (isFilled) {
            ButtonDefaults.buttonElevation()
        } else {
            ButtonDefaults.filledTonalButtonElevation()
        },
        contentPadding = size.contentPadding,
        interactionSource = interactionSource,
        modifier = modifier
            .height(height = size.height)
            .then(
                if (isFilled) {
                    Modifier.tactilePressScale(interactionSource)
                } else {
                    Modifier
                },
            )
            .animateContentSize(animationSpec = motionScheme.defaultSpatialSpec()),
    ) {
        AnimatedContent(
            targetState = label to icon,
            modifier = Modifier.clearAndSetSemantics { contentDescription = label },
            transitionSpec = {
                (
                    fadeIn(animationSpec = motionScheme.fastEffectsSpec()) togetherWith
                        fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                    ).using(
                    SizeTransform(clip = false) { _, _ -> snap() },
                )
            },
            label = "morphingButtonContent",
        ) { (text, leadingIcon) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Icon(
                        painter = leadingIcon.getIconPainter(),
                        contentDescription = leadingIcon.contentDescription,
                        modifier = Modifier.size(size.iconSize),
                    )

                    Spacer(modifier = Modifier.width(size.iconSpacing))
                }

                Text(
                    text = text,
                    style = size.textStyle,
                )
            }
        }
    }
}
