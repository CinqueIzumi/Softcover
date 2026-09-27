package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * A tag pill with a leading spoiler-toggle eye and a trailing remove ×, at opposite ends so the two
 * actions never crowd each other. Plays a brief fade + rise on the first composition where
 * [isNewlyAdded] is true — i.e. the render pass that just inserted this tag — and never re-triggers on
 * later recompositions of the same chip (its `key(category, name)` slot keeps the `remember` below alive).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TagChip(
    tag: UserTag,
    isNewlyAdded: Boolean,
    onToggleSpoiler: () -> Unit,
    onRemove: () -> Unit,
) {
    val playMotion = playDecorativeMotion()
    val density = LocalDensity.current

    val visibleState = remember {
        MutableTransitionState(initialState = (isNewlyAdded && playMotion).not())
    }

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = if (playMotion) {
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                slideInVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) {
                    with(density) { 3.dp.roundToPx() }
                }
        } else {
            EnterTransition.None
        },
        exit = ExitTransition.None,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(start = 9.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            SpoilerToggleIcon(
                spoiler = tag.spoiler,
                onClick = onToggleSpoiler,
            )

            TagChipName(
                name = tag.name,
                spoiler = tag.spoiler,
            )

            RemoveTagIcon(
                tagName = tag.name,
                onClick = onRemove,
            )
        }
    }
}

@Composable
private fun SpoilerToggleIcon(
    spoiler: Boolean,
    onClick: () -> Unit,
) {
    val description = if (spoiler) "Marked as spoiler — tap to unmark" else "Mark as spoiler"
    val icon = if (spoiler) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility
    val tint = if (spoiler) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    val resolvedIcon = drawableIconResource(
        contentDescription = description,
        icon = icon,
    )

    DesktopTooltip(text = description) {
        Icon(
            painter = resolvedIcon.getIconPainter(),
            contentDescription = resolvedIcon.contentDescription,
            tint = tint,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick)
                .padding(3.dp)
                .size(17.dp),
        )
    }
}

@Composable
private fun TagChipName(
    name: String,
    spoiler: Boolean,
) {
    val style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)

    if (spoiler) {
        Text(
            text = name,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.spoilerEditorHighlight)
                .padding(horizontal = 3.dp, vertical = 1.dp),
        )
    } else {
        Text(
            text = name,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RemoveTagIcon(
    tagName: String,
    onClick: () -> Unit,
) {
    val description = "Remove $tagName"
    val resolvedIcon = drawableIconResource(
        contentDescription = description,
        icon = SoftcoverIcon.Close,
    )

    DesktopTooltip(text = description) {
        Icon(
            painter = resolvedIcon.getIconPainter(),
            contentDescription = resolvedIcon.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick)
                .padding(5.dp)
                .size(13.dp),
        )
    }
}
