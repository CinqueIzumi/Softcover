package nl.rhaydus.softcover.core.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight

private val ChipShape = RoundedCornerShape(percent = 50)

/**
 * The pill-shaped chip family (`component-contract.md` § 7.2 R2, `component-library-migration`
 * README D11/D12): one label on a fully-rounded surface, its anatomy and colours resolved from
 * [ChipUiModel.variant]. [ChipUiModel.interaction] gates the tap affordance independently of the
 * variant, so the same [ChipVariant.Tonal] pill backs both the library's interactive filter facets
 * and book-detail's [ChipInteraction.Inert] read-only tags. Every clickable pill reports through this
 * one [ChipEvent] lambda (R1) using the same press-scale + hand-cursor affordance as `WhenReadRow`
 * and `ChooseListsRow`, never a `Surface(onClick)` ripple.
 */
@Composable
fun Chip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (val variant = model.variant) {
        is ChipVariant.Tonal -> TonalChip(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )
        ChipVariant.Spoiler -> SpoilerChip(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )
        ChipVariant.Add -> AddChip(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )
        ChipVariant.AddOutlined -> AddOutlinedChip(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )
        is ChipVariant.Remove -> RemoveChip(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )
        is ChipVariant.Quiet -> QuietChip(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )
        is ChipVariant.Format -> FormatChip(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )
        is ChipVariant.Choice -> ChoiceChip(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )
        ChipVariant.Dashed -> DashedChip(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )
        is ChipVariant.Editable -> EditableChip(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@Composable
private fun TonalChip(
    model: ChipUiModel,
    variant: ChipVariant.Tonal,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val container = if (variant.selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val content = if (variant.selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val fontWeight = if (variant.selected) FontWeight.SemiBold else FontWeight.Medium
    val dimensions = ChipDimensions.forVariant(variant)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(container)
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = content,
                dimensions = dimensions,
            )
        }

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = fontWeight),
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        model.dismissLabel?.let { label ->
            ChipDismissIcon(dismissLabel = label, tint = content, dimensions = dimensions) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun SpoilerChip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val dimensions = ChipDimensions.forVariant(ChipVariant.Spoiler)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(MaterialTheme.colorScheme.spoilerCover)
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = Color.Transparent,
                dimensions = dimensions,
            )
        }

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Transparent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        model.dismissLabel?.let { label ->
            ChipDismissIcon(dismissLabel = label, tint = Color.Transparent, dimensions = dimensions) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun AddChip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val content = MaterialTheme.colorScheme.onPrimary
    val dimensions = ChipDimensions.forVariant(ChipVariant.Add)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(MaterialTheme.colorScheme.primary)
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = content,
                dimensions = dimensions,
            )
        }

        Text(
            text = "+ ${model.label}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = content,
        )

        model.dismissLabel?.let { label ->
            ChipDismissIcon(dismissLabel = label, tint = content, dimensions = dimensions) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun AddOutlinedChip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dimensions = ChipDimensions.forVariant(ChipVariant.AddOutlined)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .border(
                width = dimensions.borderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = ChipShape,
            )
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                dimensions = dimensions,
            )
        }

        Text(
            text = "+",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.width(dimensions.innerGap))

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = labelColor,
        )

        model.dismissLabel?.let { label ->
            ChipDismissIcon(dismissLabel = label, tint = labelColor, dimensions = dimensions) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun RemoveChip(
    model: ChipUiModel,
    variant: ChipVariant.Remove,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val content = MaterialTheme.colorScheme.onPrimaryContainer
    val dimensions = ChipDimensions.forVariant(variant)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = content,
                dimensions = dimensions,
            )
        }

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = content,
        )

        Spacer(modifier = Modifier.width(dimensions.innerGap))

        val closeIcon = drawableIconResource(
            icon = SoftcoverIcon.Close,
            contentDescription = variant.removeLabel,
        )

        Icon(
            painter = closeIcon.getIconPainter(),
            contentDescription = closeIcon.contentDescription,
            tint = content,
            modifier = Modifier.size(dimensions.removeIconSize),
        )
    }
}

@Composable
private fun QuietChip(
    model: ChipUiModel,
    variant: ChipVariant.Quiet,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val container = if (variant.selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val content = if (variant.selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val dimensions = ChipDimensions.forVariant(variant)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(container)
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = content,
                dimensions = dimensions,
            )
        }

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        model.dismissLabel?.let { label ->
            ChipDismissIcon(dismissLabel = label, tint = content, dimensions = dimensions) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun FormatChip(
    model: ChipUiModel,
    variant: ChipVariant.Format,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val container = if (variant.active) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val content = if (variant.active) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val dimensions = ChipDimensions.forVariant(variant)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(container)
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = content,
                dimensions = dimensions,
            )
        }

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (variant.face == ChipFace.Bold) FontWeight.Bold else null,
                fontStyle = if (variant.face == ChipFace.Italic) FontStyle.Italic else FontStyle.Normal,
            ),
            color = content,
        )

        model.dismissLabel?.let { label ->
            ChipDismissIcon(dismissLabel = label, tint = content, dimensions = dimensions) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    model: ChipUiModel,
    variant: ChipVariant.Choice,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val container = if (variant.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val content = if (variant.selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val dimensions = ChipDimensions.forVariant(variant)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(container)
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = content,
                dimensions = dimensions,
            )
        }

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        variant.trailingIcon?.let { icon ->
            Spacer(modifier = Modifier.width(dimensions.trailingIconGap))

            val resource = drawableIconResource(
                icon = icon,
                contentDescription = "",
            )

            Icon(
                painter = resource.getIconPainter(),
                contentDescription = resource.contentDescription,
                tint = content,
                modifier = Modifier.size(dimensions.trailingIconSize),
            )
        }

        model.dismissLabel?.let { label ->
            ChipDismissIcon(dismissLabel = label, tint = content, dimensions = dimensions) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun DashedChip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    val dimensions = ChipDimensions.forVariant(ChipVariant.Dashed)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .drawBehind {
                drawRoundRect(
                    color = color,
                    style = Stroke(
                        width = dimensions.borderWidth.toPx(),
                        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6f, 4f)),
                    ),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            }
            .chipInteraction(interaction = model.interaction, dimensions = dimensions) {
                onEvent(ChipEvent.Clicked(key = model.key))
            }
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        model.leadingIcon?.let { icon ->
            ChipLeadingIcon(
                icon = icon,
                tint = color,
                dimensions = dimensions,
            )
        }

        Text(
            text = model.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Composable
private fun EditableChip(
    model: ChipUiModel,
    variant: ChipVariant.Editable,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val dimensions = ChipDimensions.forVariant(variant)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ChipShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(
                start = dimensions.paddingStart,
                top = dimensions.paddingTop,
                end = dimensions.paddingEnd,
                bottom = dimensions.paddingBottom,
            ),
    ) {
        ChipSpoilerToggleIcon(
            spoiler = variant.spoiler,
            spoilerToggleLabel = variant.spoilerToggleLabel,
            dimensions = dimensions,
            onClick = { onEvent(ChipEvent.SpoilerToggled(key = model.key)) },
        )

        ChipEditableLabel(
            label = model.label,
            spoiler = variant.spoiler,
        )

        model.dismissLabel?.let { label ->
            ChipDismissIcon(
                dismissLabel = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                dimensions = dimensions,
            ) {
                onEvent(ChipEvent.Dismissed(key = model.key))
            }
        }
    }
}

@Composable
private fun ChipSpoilerToggleIcon(
    spoiler: Boolean,
    spoilerToggleLabel: String,
    dimensions: ChipDimensions,
    onClick: () -> Unit,
) {
    val icon = if (spoiler) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility
    val tint = if (spoiler) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    val resource = drawableIconResource(
        icon = icon,
        contentDescription = spoilerToggleLabel,
    )

    Icon(
        painter = resource.getIconPainter(),
        contentDescription = resource.contentDescription,
        tint = tint,
        modifier = Modifier
            .size(dimensions.leadingIconSize)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
    )

    Spacer(modifier = Modifier.width(dimensions.leadingIconGap))
}

@Composable
private fun ChipEditableLabel(
    label: String,
    spoiler: Boolean,
) {
    val style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)

    if (spoiler) {
        Text(
            text = label,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.spoilerEditorHighlight)
                .padding(horizontal = 3.dp, vertical = 1.dp),
        )
    } else {
        Text(
            text = label,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun Modifier.chipInteraction(
    interaction: ChipInteraction,
    dimensions: ChipDimensions,
    onClick: () -> Unit,
): Modifier = when (interaction) {
    ChipInteraction.Clickable -> pointerHandCursor().pressScaleClickable(onClick = onClick)
    ChipInteraction.Disabled -> alpha(dimensions.disabledAlpha)
    ChipInteraction.Inert -> this
}

@Composable
private fun ChipLeadingIcon(
    icon: SoftcoverIcon,
    tint: Color,
    dimensions: ChipDimensions,
) {
    val resource = drawableIconResource(
        icon = icon,
        contentDescription = "",
    )

    Icon(
        painter = resource.getIconPainter(),
        contentDescription = resource.contentDescription,
        tint = tint,
        modifier = Modifier.size(dimensions.leadingIconSize),
    )

    Spacer(modifier = Modifier.width(dimensions.leadingIconGap))
}

@Composable
private fun ChipDismissIcon(
    dismissLabel: String,
    tint: Color,
    dimensions: ChipDimensions,
    onDismiss: () -> Unit,
) {
    Spacer(modifier = Modifier.width(dimensions.dismissIconGap))

    val resource = drawableIconResource(
        icon = SoftcoverIcon.Close,
        contentDescription = dismissLabel,
    )

    Icon(
        painter = resource.getIconPainter(),
        contentDescription = resource.contentDescription,
        tint = tint,
        modifier = Modifier
            .size(dimensions.dismissIconSize)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onDismiss),
    )
}
