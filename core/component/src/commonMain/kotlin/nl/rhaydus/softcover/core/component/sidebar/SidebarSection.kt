package nl.rhaydus.softcover.core.component.sidebar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

private object SidebarRowMetrics {
    val OuterPaddingHorizontal = 12.dp
    val OuterPaddingVertical = 2.dp
    val InnerPaddingHorizontal = 14.dp
    val InnerPaddingVertical = 11.dp
    val Shape = RoundedCornerShape(10.dp)
    val IconSize = 20.dp
    val IconGap = 14.dp
    val ArrowSize = 18.dp
}

private val SidebarLabelInsetStart = 26.dp
private val SidebarLabelInsetTop = 8.dp
private val SidebarLabelInsetBottom = 6.dp
private val SidebarSectionGap = 16.dp

/** Renders every section in [model] under one shared `selectableGroup()`, with a gap between sections. */
@Composable
fun <K> Sidebar(
    model: SidebarUiModel<K>,
    onEvent: (SidebarEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.selectableGroup()) {
        model.sections.forEachIndexed { index, section ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(SidebarSectionGap))
            }

            SidebarSection(
                model = section,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun <K> SidebarSection(
    model: SidebarSectionUiModel<K>,
    onEvent: (SidebarEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (model.label != null) {
            SectionHeader(
                model = model.label,
                modifier = Modifier.padding(
                    start = SidebarLabelInsetStart,
                    top = SidebarLabelInsetTop,
                    bottom = SidebarLabelInsetBottom,
                ),
            )
        }

        model.rows.forEach { row ->
            SidebarRow(
                model = row,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <K> SidebarRow(
    model: SidebarRowUiModel<K>,
    onEvent: (SidebarEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = LocalHaptics.current
    val variant = model.variant
    val isSelected = (variant as? SidebarRowVariant.Destination)?.selected ?: false
    val rowRole = if (variant is SidebarRowVariant.Destination) Role.Tab else Role.Button
    val onLongClick: (() -> Unit)? = if (variant is SidebarRowVariant.Destination && variant.longPressable) {
        {
            haptics.threshold()
            onEvent(SidebarEvent.LongPressed(key = model.key))
        }
    } else {
        null
    }

    val container = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val content = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SidebarRowMetrics.OuterPaddingHorizontal,
                vertical = SidebarRowMetrics.OuterPaddingVertical,
            )
            .pointerHandCursor()
            .hoverHighlight(
                interactionSource = interactionSource,
                shape = SidebarRowMetrics.Shape,
            )
            .semantics {
                role = rowRole
                if (variant is SidebarRowVariant.Destination) {
                    selected = variant.selected
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onEvent(SidebarEvent.Clicked(key = model.key)) },
                onLongClick = onLongClick,
            ),
        color = container,
        contentColor = content,
        shape = SidebarRowMetrics.Shape,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = SidebarRowMetrics.InnerPaddingHorizontal,
                vertical = SidebarRowMetrics.InnerPaddingVertical,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (model.icon != null) {
                val leadingIcon = drawableIconResource(
                    icon = model.icon,
                    contentDescription = "",
                )

                Icon(
                    painter = leadingIcon.getIconPainter(),
                    contentDescription = leadingIcon.contentDescription,
                    modifier = Modifier.size(SidebarRowMetrics.IconSize),
                )

                Spacer(modifier = Modifier.width(SidebarRowMetrics.IconGap))
            }

            SoftcoverText(
                text = model.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )

            if (variant is SidebarRowVariant.Push) {
                val arrowIcon = drawableIconResource(
                    icon = SoftcoverIcon.KeyboardArrowRight,
                    contentDescription = "",
                )

                Icon(
                    painter = arrowIcon.getIconPainter(),
                    contentDescription = arrowIcon.contentDescription,
                    modifier = Modifier.size(SidebarRowMetrics.ArrowSize),
                )
            }
        }
    }
}
