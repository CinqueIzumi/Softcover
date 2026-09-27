package nl.rhaydus.softcover.feature.book_detail.presentation.component

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * The naming pill: a tone-on-tone `surfaceContainerHigh` field with a leading edit glyph and a
 * borderless inline field — reads as naming, not a form box. `EditorialSearchField` hardwires a search
 * glyph and a trailing clear button and has no leading-edit / trailing-commit slot, so this is composed
 * from the same primitives instead of bending that component.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TagNamingField(
    draft: String,
    placeholder: String,
    onDraftChange: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val canCommit = draft.trim().isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(
                start = 14.dp,
                end = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val editIcon = drawableIconResource(
            contentDescription = "",
            icon = SoftcoverIcon.Edit,
        )

        Icon(
            painter = editIcon.getIconPainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (draft.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }

            BasicTextField(
                value = draft,
                onValueChange = onDraftChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onCommit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AnimatedVisibility(
            visible = canCommit,
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
            AddPill(onClick = onCommit)
        }
    }
}

@Composable
private fun AddPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(percent = 50),
    ) {
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
