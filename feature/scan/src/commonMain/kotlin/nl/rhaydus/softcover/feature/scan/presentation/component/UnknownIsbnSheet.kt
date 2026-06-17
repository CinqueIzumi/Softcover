package nl.rhaydus.softcover.feature.scan.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun UnknownIsbnSheet(
    isbn: String,
    isAdding: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = { if (isAdding.not()) onDismiss() }) {
        val dismiss = LocalModalSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Not in Hardcover",
                headline = "We don't have this one yet.",
                description = "Double-check the ISBN below — if it's right, add it to Hardcover so you can track it.",
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = isbn,
                        // Wider tracking so the digit string reads as a scannable code, not prose.
                        style = MaterialTheme.editorialTypography.titleLarge.copy(letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            RhaydusButton(
                label = if (isAdding) "Adding to Hardcover" else "Add to Hardcover",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                enabled = isAdding.not(),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            RhaydusButton(
                label = "Cancel",
                style = ButtonStyle.TEXT,
                size = ButtonSize.M,
                enabled = isAdding.not(),
                onClick = dismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
