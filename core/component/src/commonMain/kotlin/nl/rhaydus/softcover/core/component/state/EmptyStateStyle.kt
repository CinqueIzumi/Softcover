package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.EditorialTypography

@Immutable
internal data class EmptyStateStyle(
    val showsGlyph: Boolean,
    val titleStyle: TextStyle,
    val bodyStyle: TextStyle,
    val textAlign: TextAlign,
    val bodyGap: Dp,
    val horizontalAlignment: Alignment.Horizontal,
    val verticalArrangement: Arrangement.Vertical,
)

internal fun emptyStateStyle(
    variant: EmptyStateVariant,
    editorialTypography: EditorialTypography,
): EmptyStateStyle = when (variant) {
    is EmptyStateVariant.Centered -> EmptyStateStyle(
        showsGlyph = true,
        titleStyle = editorialTypography.headlineMedium,
        bodyStyle = editorialTypography.bodyLarge,
        textAlign = TextAlign.Center,
        bodyGap = 12.dp,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    )

    EmptyStateVariant.Inline -> EmptyStateStyle(
        showsGlyph = false,
        titleStyle = editorialTypography.headlineSmall,
        bodyStyle = editorialTypography.body,
        textAlign = TextAlign.Start,
        bodyGap = 4.dp,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    )
}
