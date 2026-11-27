package nl.rhaydus.softcover.core.component.field

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import nl.rhaydus.designsystem.theme.Corners

internal val outlinedFieldShape: Shape = Corners.sm

@Composable
internal fun outlinedFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors().copy(
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
)
