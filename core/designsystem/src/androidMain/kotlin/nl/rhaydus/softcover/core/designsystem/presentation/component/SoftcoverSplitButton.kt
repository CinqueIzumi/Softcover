package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.SplitButtonShapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverMenuItem
import nl.rhaydus.softcover.core.designsystem.presentation.model.SplitButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview

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
    fillMaxWidth: Boolean = false,
) {
    // Workaround for a Material 3 (1.5.0-alpha13) bug: `SplitButtonDefaults.*ShapesFor`
    // returns shapes whose outer corners use a percentage `CornerSize` (50%). Inside
    // `rememberAnimatedShape`, those percentage corners are first resolved with
    // `size = Size.Zero` during the `horizontalCenterOptically` measure pass, so the
    // backing `Animatable`s initialise at 0 px. The first frame paints the trailing
    // button with straight outer edges before the side-effect kicks in and animates the
    // corners to their real radius. We rebuild the shape sets with dp-based outer
    // corners (half the button height) so every corner is size-independent.
    val corners = softcoverCornersFor(buttonHeight = size.height)
    val leadingButtonShapes = leadingShapesFrom(corners)
    val leadingButtonContentPadding =
        SplitButtonDefaults.leadingButtonContentPaddingFor(buttonHeight = size.height)

    val trailingButtonShapes = trailingShapesFrom(corners)
    val trailingButtonContentPadding =
        SplitButtonDefaults.trailingButtonContentPaddingFor(buttonHeight = size.height)

    val leadingContent: @Composable RowScope.() -> Unit = {
        leadingIcon?.let { icon ->
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                modifier = Modifier.size(size.iconSize),
            )

            Spacer(modifier = Modifier.width(size.iconSpacing))
        }

        Text(
            text = label,
            style = size.textStyle,
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
                },
        )
    }

    val leadingButton: @Composable (Modifier) -> Unit = { modifier ->
        when (leadingButtonStyle) {
            SplitButtonStyle.FILLED -> SplitButtonDefaults.LeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = modifier,
                content = leadingContent,
                enabled = leadingEnabled,
            )

            SplitButtonStyle.TONAL -> SplitButtonDefaults.TonalLeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = modifier,
                content = leadingContent,
                enabled = leadingEnabled,
            )

            SplitButtonStyle.ELEVATED -> SplitButtonDefaults.ElevatedLeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = modifier,
                content = leadingContent,
                enabled = leadingEnabled,
            )

            SplitButtonStyle.OUTLINED -> SplitButtonDefaults.OutlinedLeadingButton(
                onClick = onLeadingButtonClick,
                shapes = leadingButtonShapes,
                contentPadding = leadingButtonContentPadding,
                modifier = modifier,
                content = leadingContent,
                enabled = leadingEnabled,
            )
        }
    }

    val trailingButton: @Composable (Modifier) -> Unit = { modifier ->
        when (trailingButtonStyle) {
            SplitButtonStyle.FILLED -> SplitButtonDefaults.TrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = modifier,
                content = trailingContent,
                enabled = trailingEnabled,
            )

            SplitButtonStyle.TONAL -> SplitButtonDefaults.TonalTrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = modifier,
                content = trailingContent,
                enabled = trailingEnabled,
            )

            SplitButtonStyle.ELEVATED -> SplitButtonDefaults.ElevatedTrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = modifier,
                content = trailingContent,
                enabled = trailingEnabled,
            )

            SplitButtonStyle.OUTLINED -> SplitButtonDefaults.OutlinedTrailingButton(
                checked = checked,
                onCheckedChange = onTrailingButtonClick,
                shapes = trailingButtonShapes,
                contentPadding = trailingButtonContentPadding,
                modifier = modifier,
                content = trailingContent,
                enabled = trailingEnabled,
            )
        }
    }

    val buttonHeightModifier = Modifier.height(height = size.height)

    val dropdownMenu: @Composable () -> Unit = {
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
                            contentDescription = item.icon.contentDescription,
                        )
                    },
                )
            }
        }
    }

    if (fillMaxWidth) {
        Row(modifier = Modifier.fillMaxWidth()) {
            leadingButton(
                Modifier
                    .weight(weight = 1f)
                    .height(height = size.height)
                    .padding(end = SplitButtonDefaults.Spacing),
            )

            Box {
                trailingButton(buttonHeightModifier)

                dropdownMenu()
            }
        }
    } else {
        Box {
            SplitButtonLayout(
                leadingButton = { leadingButton(buttonHeightModifier) },
                trailingButton = { trailingButton(buttonHeightModifier) },
            )

            dropdownMenu()
        }
    }
}

private data class SoftcoverSplitButtonCorners(
    val outer: Dp,
    val inner: Dp,
    val innerPressed: Dp,
)

private fun softcoverCornersFor(buttonHeight: Dp): SoftcoverSplitButtonCorners {
    val outer = buttonHeight / 2

    return when (buttonHeight) {
        ButtonSize.XS.height -> SoftcoverSplitButtonCorners(
            outer = outer,
            inner = 4.dp,
            innerPressed = 8.dp,
        )
        ButtonSize.S.height -> SoftcoverSplitButtonCorners(
            outer = outer,
            inner = 4.dp,
            innerPressed = 12.dp,
        )
        ButtonSize.M.height -> SoftcoverSplitButtonCorners(
            outer = outer,
            inner = 4.dp,
            innerPressed = 12.dp,
        )
        ButtonSize.L.height -> SoftcoverSplitButtonCorners(
            outer = outer,
            inner = 8.dp,
            innerPressed = 20.dp,
        )
        ButtonSize.XL.height -> SoftcoverSplitButtonCorners(
            outer = outer,
            inner = 12.dp,
            innerPressed = 20.dp,
        )
        else -> SoftcoverSplitButtonCorners(
            outer = outer,
            inner = 4.dp,
            innerPressed = 8.dp,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun leadingShapesFrom(corners: SoftcoverSplitButtonCorners): SplitButtonShapes = SplitButtonShapes(
    shape = RoundedCornerShape(
        topStart = corners.outer,
        topEnd = corners.inner,
        bottomEnd = corners.inner,
        bottomStart = corners.outer,
    ),
    pressedShape = RoundedCornerShape(
        topStart = corners.outer,
        topEnd = corners.innerPressed,
        bottomEnd = corners.innerPressed,
        bottomStart = corners.outer,
    ),
    checkedShape = null,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun trailingShapesFrom(corners: SoftcoverSplitButtonCorners): SplitButtonShapes = SplitButtonShapes(
    shape = RoundedCornerShape(
        topStart = corners.inner,
        topEnd = corners.outer,
        bottomEnd = corners.outer,
        bottomStart = corners.inner,
    ),
    pressedShape = RoundedCornerShape(
        topStart = corners.innerPressed,
        topEnd = corners.outer,
        bottomEnd = corners.outer,
        bottomStart = corners.innerPressed,
    ),
    checkedShape = RoundedCornerShape(
        topStart = corners.outer,
        topEnd = corners.outer,
        bottomEnd = corners.outer,
        bottomStart = corners.outer,
    ),
)

@StandardPreview
@Composable
private fun SoftcoverSplitButtonPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
        ) {
            val leadingIcon = SoftcoverIconResource.Drawable(
                icon = SoftcoverIcon.Palette,
                contentDescription = "",
            )

            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverSplitButton(
                    size = size,
                    label = "Label $index",
                    trailingIcon = SoftcoverIconResource.Drawable(
                        icon = SoftcoverIcon.ArrowDropDown,
                        contentDescription = "",
                    ),
                    leadingIcon = if (index % 2 == 0) leadingIcon else null,
                    onLeadingButtonClick = {},
                    onTrailingButtonClick = {},
                    leadingButtonStyle = SplitButtonStyle.FILLED,
                    checked = index % 2 == 0,
                    onDismissMenuRequest = {},
                    dropDownItems = emptyList(),
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
