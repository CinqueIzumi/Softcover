package nl.rhaydus.softcover.core.component.traits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.modifier.tactileClickable
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The read-only "How it read" panel on the book page. Its eyebrow is a header the caller places above it.
 *
 * [HowItReadBlockUiModel.Empty] is one tappable invitation; [HowItReadBlockUiModel.Filled] is a summary row
 * that opens its rows and an "Edit" link. Answers are only ever changed in [HowItReadSheet], which the
 * caller opens on [HowItReadBlockEvent.EditRequested].
 */
@Composable
fun HowItReadBlock(
    model: HowItReadBlockUiModel,
    onEvent: (HowItReadBlockEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Corners.lg,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        when (model) {
            is HowItReadBlockUiModel.Empty -> HowItReadInvitation(
                invitation = model.invitation,
                onClick = { onEvent(HowItReadBlockEvent.EditRequested) },
            )

            is HowItReadBlockUiModel.Filled -> HowItReadFilled(
                model = model,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun HowItReadInvitation(
    invitation: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .pointerHandCursor()
            .tactileClickable(onClick = onClick)
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftcoverText(
            text = invitation,
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HowItReadFilled(
    model: HowItReadBlockUiModel.Filled,
    onEvent: (HowItReadBlockEvent) -> Unit,
) {
    Column {
        HowItReadSummaryRow(
            model = model,
            onClick = { onEvent(HowItReadBlockEvent.ExpandToggled) },
        )

        AnimatedVisibility(
            visible = model.expanded,
            enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        ) {
            HowItReadRows(
                rows = model.rows,
                editLabel = model.editLabel,
                onEditClick = { onEvent(HowItReadBlockEvent.EditRequested) },
            )
        }
    }
}

@Composable
private fun HowItReadSummaryRow(
    model: HowItReadBlockUiModel.Filled,
    onClick: () -> Unit,
) {
    val playMotion = playDecorativeMotion()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val rotation by animateFloatAsState(
        targetValue = if (model.expanded) 180f else 0f,
        animationSpec = if (playMotion) spatialSpec else snap(),
        label = "HowItReadChevronRotation",
    )
    val chevron = drawableIconResource(
        icon = SoftcoverIcon.ArrowDropDown,
        contentDescription = model.toggleLabel,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .pointerHandCursor()
            .tactileClickable(onClick = onClick)
            .padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftcoverText(
            text = model.summary,
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Icon(
            painter = chevron.getIconPainter(),
            contentDescription = chevron.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = rotation },
        )
    }
}

@Composable
private fun HowItReadRows(
    rows: ImmutableList<HowItReadRowUiModel>,
    editLabel: String,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.md,
                end = Spacing.md,
                bottom = Spacing.sm,
            ),
    ) {
        rows.forEach { row ->
            HowItReadRow(row = row)
        }

        Box(
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = 48.dp)
                .pointerHandCursor()
                .tactileClickable(
                    onClick = onEditClick,
                    role = Role.Button,
                )
                .padding(horizontal = Spacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            SoftcoverText(
                text = editLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
