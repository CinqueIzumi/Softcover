package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.model.SoftcoverMenuItem
import nl.rhaydus.softcover.core.presentation.model.SplitButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverSplitButton(
    checked: Boolean,
    dropDownItems: List<SoftcoverMenuItem>,
    label: String,
    trailingIcon: SoftcoverIconResource,
    onDismissMenuRequest: () -> Unit,
    onLeadingButtonClick: () -> Unit,
    onTrailingButtonClick: (Boolean) -> Unit,
    size: ButtonSize = ButtonSize.S,
    leadingButtonStyle: SplitButtonStyle,
    leadingEnabled: Boolean = true,
    trailingButtonStyle: SplitButtonStyle = leadingButtonStyle,
    trailingEnabled: Boolean = true,
    leadingIcon: SoftcoverIconResource? = null,
) {
    val leadingButtonShapes =
        SplitButtonDefaults.leadingButtonShapesFor(buttonHeight = size.height)
    val leadingButtonContentPadding =
        SplitButtonDefaults.leadingButtonContentPaddingFor(buttonHeight = size.height)

    val trailingButtonShapes =
        SplitButtonDefaults.trailingButtonShapesFor(buttonHeight = size.height)
    val trailingButtonContentPadding =
        SplitButtonDefaults.trailingButtonContentPaddingFor(buttonHeight = size.height)

    val leadingContent: @Composable RowScope.() -> Unit = {
        leadingIcon?.let { icon ->
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                modifier = Modifier.size(size.iconSize)
            )

            Spacer(modifier = Modifier.width(size.iconSpacing))
        }

        Text(
            text = label,
            style = size.textStyle
        )
    }

    val rotation: Float by animateFloatAsState(
        targetValue = if (checked) 180f else 0f,
        label = "Trailing Icon Rotation",
    )

    val trailingContent: @Composable RowScope.() -> Unit = {
        Icon(
            painter = trailingIcon.getIconPainter(),
            contentDescription = trailingIcon.contentDescription,
            modifier = Modifier
                .size(size.iconSize)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )
    }

    val leadingButton: @Composable () -> Unit = {
        when (leadingButtonStyle) {
            SplitButtonStyle.FILLED -> SplitButtonDefaults.LeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = leadingContent,
                enabled = leadingEnabled,
            )

            SplitButtonStyle.TONAL -> SplitButtonDefaults.TonalLeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = leadingContent,
                enabled = leadingEnabled,
            )

            SplitButtonStyle.ELEVATED -> SplitButtonDefaults.ElevatedLeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = leadingContent,
                enabled = leadingEnabled,
            )

            SplitButtonStyle.OUTLINED -> SplitButtonDefaults.OutlinedLeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = leadingContent,
                enabled = leadingEnabled,
            )
        }
    }

    val trailingButton: @Composable () -> Unit = {
        when (trailingButtonStyle) {
            SplitButtonStyle.FILLED -> SplitButtonDefaults.TrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = trailingContent,
                enabled = trailingEnabled,
            )

            SplitButtonStyle.TONAL -> SplitButtonDefaults.TonalTrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = trailingContent,
                enabled = trailingEnabled,
            )

            SplitButtonStyle.ELEVATED -> SplitButtonDefaults.ElevatedTrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = trailingContent,
                enabled = trailingEnabled,
            )

            SplitButtonStyle.OUTLINED -> SplitButtonDefaults.OutlinedTrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = Modifier.height(height = size.height),
                content = trailingContent,
                enabled = trailingEnabled,
            )
        }
    }

    Box {
        SplitButtonLayout(
            leadingButton = leadingButton,
            trailingButton = trailingButton,
        )

        DropdownMenu(
            expanded = checked,
            onDismissRequest = onDismissMenuRequest,
        ) {
            dropDownItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.label) },
                    onClick = item.onClick,
                    leadingIcon = {
                        Icon(
                            painter = item.icon.getIconPainter(),
                            contentDescription = item.icon.contentDescription
                        )
                    }
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverSplitButtonPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp)
        ) {
            val leadingIcon = SoftcoverIconResource.Vector(
                vector = Icons.Default.Edit,
                contentDescription = "",
            )

            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverSplitButton(
                    size = size,
                    label = "Label $index",
                    trailingIcon = SoftcoverIconResource.Vector(
                        vector = Icons.Default.ArrowDropDown,
                        contentDescription = "",
                    ),
                    leadingIcon = if (index % 2 == 0) leadingIcon else null,
                    onLeadingButtonClick = {},
                    onTrailingButtonClick = {},
                    leadingButtonStyle = SplitButtonStyle.FILLED,
                    checked = index % 2 == 0,
                    onDismissMenuRequest = {},
                    dropDownItems = emptyList()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}