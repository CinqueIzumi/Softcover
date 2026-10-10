package nl.rhaydus.softcover.feature.scan.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.scan.presentation.state.UnknownIsbnSheetUiModel

@Composable
internal fun UnknownIsbnSheet(
    model: UnknownIsbnSheetUiModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model.scaffold,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onConfirm()

                SheetScaffoldEvent.Dismissed -> onDismiss()

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        content = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = Corners.lg,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    SoftcoverText(
                        text = model.isbn,
                        // Wider tracking so the digit string reads as a scannable code, not prose.
                        style = MaterialTheme.editorialTypography.titleLarge.copy(letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}
