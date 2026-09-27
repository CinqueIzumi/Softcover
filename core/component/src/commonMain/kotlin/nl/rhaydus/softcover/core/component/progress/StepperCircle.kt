package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A −/+ stepper flanking a hero number field: a 44dp circle, `surfaceContainerHigh` fill, primary
 * glyph. On the Page and Percentage tabs it steps the value by 1, reusing the same clamp bounds
 * (`[0, total]` / `[0, 100]`) the typed-entry path already enforces; on the Time tab it flanks only
 * the minutes field, stepping by 1 minute (hours and seconds stay typed-entry only). A tap writes
 * straight into the tab's existing `TextFieldValue` state, so it composes with typed entry rather
 * than branching around it.
 */
@Composable
internal fun StepperCircle(
    symbol: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(44.dp)
            // The visible "−"/"+" glyph would otherwise merge into the announced label alongside
            // the content description; clear it and re-declare only what a screen reader should
            // say — the description plus the button role/action Surface's own onClick provides.
            .clearAndSetSemantics {
                role = Role.Button
                this.contentDescription = contentDescription
                onClick(label = null) {
                    onClick()
                    true
                }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}
