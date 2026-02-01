package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.model.IconToggleButtonShape
import nl.rhaydus.softcover.core.presentation.model.IconToggleButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.model.ToggleButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverIconToggleButton(
    checked: Boolean,
    icon: SoftcoverIconResource,
    style: IconToggleButtonStyle,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: IconToggleButtonShape = IconToggleButtonShape.ROUND,
    size: ButtonSize = ButtonSize.S,
) {
    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                modifier = Modifier.size(size.iconSize),
            )
        }
    }

    val roundShapeDefault = shape == IconToggleButtonShape.ROUND

    val shapes = when (size) {
        ButtonSize.XS -> {
            val roundShape = IconButtonDefaults.extraSmallRoundShape
            val squareShape = IconButtonDefaults.extraSmallSquareShape

            IconButtonDefaults.toggleableShapes(
                shape = if (roundShapeDefault) roundShape else squareShape,
                checkedShape = if (roundShapeDefault) squareShape else roundShape,
                pressedShape = IconButtonDefaults.extraSmallPressedShape
            )
        }

        ButtonSize.S -> {
            val roundShape = IconButtonDefaults.smallRoundShape
            val squareShape = IconButtonDefaults.smallSquareShape

            IconButtonDefaults.toggleableShapes(
                shape = if (roundShapeDefault) roundShape else squareShape,
                checkedShape = if (roundShapeDefault) squareShape else roundShape,
                pressedShape = IconButtonDefaults.smallPressedShape
            )
        }

        ButtonSize.M -> {
            val roundShape = IconButtonDefaults.mediumRoundShape
            val squareShape = IconButtonDefaults.mediumSquareShape

            IconButtonDefaults.toggleableShapes(
                shape = if (roundShapeDefault) roundShape else squareShape,
                checkedShape = if (roundShapeDefault) squareShape else roundShape,
                pressedShape = IconButtonDefaults.mediumPressedShape
            )
        }

        ButtonSize.L -> {
            val roundShape = IconButtonDefaults.largeRoundShape
            val squareShape = IconButtonDefaults.largeSquareShape

            IconButtonDefaults.toggleableShapes(
                shape = if (roundShapeDefault) roundShape else squareShape,
                checkedShape = if (roundShapeDefault) squareShape else roundShape,
                pressedShape = IconButtonDefaults.largePressedShape
            )
        }

        ButtonSize.XL -> {
            val roundShape = IconButtonDefaults.extraLargeRoundShape
            val squareShape = IconButtonDefaults.extraLargeSquareShape

            IconButtonDefaults.toggleableShapes(
                shape = if (roundShapeDefault) roundShape else squareShape,
                checkedShape = if (roundShapeDefault) squareShape else roundShape,
                pressedShape = IconButtonDefaults.extraLargePressedShape
            )
        }
    }

    when (style) {
        IconToggleButtonStyle.FILLED -> FilledIconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.size(size.height),
            content = content,
            shapes = shapes,
            enabled = enabled,
        )

        IconToggleButtonStyle.TONAL -> FilledTonalIconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.size(size.height),
            content = content,
            shapes = shapes,
            enabled = enabled,
        )

        IconToggleButtonStyle.OUTLINED -> OutlinedIconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.size(size.height),
            content = content,
            shapes = shapes,
            enabled = enabled,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverToggleButton(
    checked: Boolean,
    label: String,
    style: ToggleButtonStyle,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.S,
) {
    val shapes = ToggleButtonDefaults.shapesFor(buttonHeight = size.height)

    val content: @Composable RowScope.() -> Unit = {
        Text(
            text = label,
            style = size.textStyle,
        )
    }

    when (style) {
        ToggleButtonStyle.FILLED -> ToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )

        ToggleButtonStyle.TONAL -> TonalToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )

        ToggleButtonStyle.ELEVATED -> ElevatedToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )

        ToggleButtonStyle.OUTLINED -> OutlinedToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverButton(
    label: String,
    style: ButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: SoftcoverIconResource? = null,
    size: ButtonSize = ButtonSize.S,
) {
    val shapes = ButtonDefaults.shapesFor(buttonHeight = size.height)

    val content: @Composable RowScope.() -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null && style != ButtonStyle.TEXT) {
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
    }

    when (style) {
        ButtonStyle.FILLED -> Button(
            onClick = onClick,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )

        ButtonStyle.TONAL -> FilledTonalButton(
            onClick = onClick,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )

        ButtonStyle.ELEVATED -> ElevatedButton(
            onClick = onClick,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )

        ButtonStyle.OUTLINED -> OutlinedButton(
            onClick = onClick,
            shapes = shapes,
            enabled = enabled,
            contentPadding = size.contentPadding,
            modifier = modifier.height(height = size.height),
            content = content,
        )

        ButtonStyle.TEXT -> TextButton(
            onClick = onClick,
            shapes = shapes,
            enabled = enabled,
            modifier = modifier.height(height = size.height),
            content = content,
        )
    }
}

@StandardPreview
@Composable
private fun SoftcoverButtonFilledPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverButton(
                    style = ButtonStyle.FILLED,
                    size = size,
                    onClick = {},
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverButtonTonalPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverButton(
                    style = ButtonStyle.TONAL,
                    size = size,
                    onClick = {},
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverButtonElevatedPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverButton(
                    style = ButtonStyle.ELEVATED,
                    size = size,
                    onClick = {},
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverButtonOutlinedPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverButton(
                    style = ButtonStyle.OUTLINED,
                    size = size,
                    onClick = {},
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverButtonTextPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverButton(
                    style = ButtonStyle.TEXT,
                    size = size,
                    onClick = {},
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverToggleButtonFilledPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverToggleButton(
                    style = ToggleButtonStyle.FILLED,
                    size = size,
                    onCheckedChange = {},
                    checked = true,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )

                SoftcoverToggleButton(
                    style = ToggleButtonStyle.FILLED,
                    size = size,
                    onCheckedChange = {},
                    checked = false,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverToggleButtonTonalPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverToggleButton(
                    style = ToggleButtonStyle.TONAL,
                    size = size,
                    onCheckedChange = {},
                    checked = true,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )

                SoftcoverToggleButton(
                    style = ToggleButtonStyle.TONAL,
                    size = size,
                    onCheckedChange = {},
                    checked = false,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverToggleButtonElevatedPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverToggleButton(
                    style = ToggleButtonStyle.ELEVATED,
                    size = size,
                    onCheckedChange = {},
                    checked = true,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )

                SoftcoverToggleButton(
                    style = ToggleButtonStyle.ELEVATED,
                    size = size,
                    onCheckedChange = {},
                    checked = false,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverToggleButtonOutlinedPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                SoftcoverToggleButton(
                    style = ToggleButtonStyle.OUTLINED,
                    size = size,
                    onCheckedChange = {},
                    checked = true,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )

                SoftcoverToggleButton(
                    style = ToggleButtonStyle.OUTLINED,
                    size = size,
                    onCheckedChange = {},
                    checked = false,
                    enabled = index % 2 == 0,
                    label = "Label $index"
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverIconToggleButtonFilledPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                Row {
                    SoftcoverIconToggleButton(
                        icon = SoftcoverIconResource.Vector(
                            vector = Icons.Default.PlayArrow,
                            contentDescription = "",
                        ),
                        size = size,
                        onCheckedChange = {},
                        checked = true,
                        enabled = index % 2 == 0,
                        style = IconToggleButtonStyle.FILLED,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    SoftcoverIconToggleButton(
                        icon = SoftcoverIconResource.Vector(
                            vector = Icons.Default.PlayArrow,
                            contentDescription = "",
                        ),
                        size = size,
                        onCheckedChange = {},
                        checked = false,
                        enabled = index % 2 == 0,
                        style = IconToggleButtonStyle.FILLED
                    )
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverIconToggleButtonTonalPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                Row {
                    SoftcoverIconToggleButton(
                        icon = SoftcoverIconResource.Vector(
                            vector = Icons.Default.PlayArrow,
                            contentDescription = "",
                        ),
                        size = size,
                        onCheckedChange = {},
                        checked = true,
                        enabled = index % 2 == 0,
                        style = IconToggleButtonStyle.TONAL
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    SoftcoverIconToggleButton(
                        icon = SoftcoverIconResource.Vector(
                            vector = Icons.Default.PlayArrow,
                            contentDescription = "",
                        ),
                        size = size,
                        onCheckedChange = {},
                        checked = false,
                        enabled = index % 2 == 0,
                        style = IconToggleButtonStyle.TONAL
                    )
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun SoftcoverIconToggleButtonOutlinedPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonSize.entries.forEachIndexed { index, size ->
                Row {
                    SoftcoverIconToggleButton(
                        icon = SoftcoverIconResource.Vector(
                            vector = Icons.Default.PlayArrow,
                            contentDescription = "",
                        ),
                        size = size,
                        onCheckedChange = {},
                        checked = true,
                        enabled = index % 2 == 0,
                        style = IconToggleButtonStyle.OUTLINED
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    SoftcoverIconToggleButton(
                        icon = SoftcoverIconResource.Vector(
                            vector = Icons.Default.PlayArrow,
                            contentDescription = "",
                        ),
                        size = size,
                        onCheckedChange = {},
                        checked = false,
                        enabled = index % 2 == 0,
                        style = IconToggleButtonStyle.OUTLINED
                    )
                }
            }
        }
    }
}