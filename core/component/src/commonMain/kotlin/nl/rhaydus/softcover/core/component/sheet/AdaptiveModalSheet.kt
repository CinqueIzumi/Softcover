package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.designsystem.theme.Corners
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.snackbar.LocalSnackbarHostLevel
import nl.rhaydus.softcover.core.component.snackbar.SoftcoverSnackbarHost
import nl.rhaydus.designsystem.component.AdaptiveModalSheet as FoundationAdaptiveModalSheet

/**
 * The height, in pixels, the bottom sheet's snackbar keeps clear at the bottom: a [SheetScaffold] footer
 * reports it so the snackbar floats above the buttons. Null outside a bottom sheet.
 */
internal val LocalSheetFooterInset = compositionLocalOf<MutableIntState?> { null }

/**
 * The foundation adaptive modal sheet with the app's `Corners.xxl` panel and its snackbar host. Each sheet
 * window hosts the snackbar, so callers never add their own. On the bottom sheet it floats over the
 * content, above a [SheetScaffold] footer, so the sheet never resizes for it.
 */
@Composable
fun AdaptiveModalSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnTapOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetLevel = LocalSnackbarHostLevel.current + 1
    val footerInset = remember { mutableIntStateOf(0) }

    FoundationAdaptiveModalSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dismissOnTapOutside = dismissOnTapOutside,
        dismissOnBackPress = dismissOnBackPress,
        panelShape = Corners.xxl,
        overlay = {
            CompositionLocalProvider(LocalSnackbarHostLevel provides sheetLevel) {
                SheetSnackbar(footerInset = footerInset)
            }
        },
    ) {
        val reportsFooter = LocalModalSheetForm.current == ModalSheetForm.SHEET

        CompositionLocalProvider(
            LocalSnackbarHostLevel provides sheetLevel,
            LocalSheetFooterInset provides if (reportsFooter) footerInset else null,
        ) {
            content()
        }
    }
}

/** The panel sits under the system bars, so it adds the navigation-bar inset; the bottom sheet already applies it. */
@Composable
private fun BoxScope.SheetSnackbar(footerInset: MutableIntState) {
    val isPanel = LocalModalSheetForm.current == ModalSheetForm.PANEL

    SoftcoverSnackbarHost(
        modifier = Modifier
            .align(if (isPanel) Alignment.BottomEnd else Alignment.BottomCenter)
            .offset {
                IntOffset(
                    x = 0,
                    y = -footerInset.intValue,
                )
            }
            .widthIn(max = 420.dp),
        snackbarModifier = if (isPanel) {
            Modifier
                .navigationBarsPadding()
                .padding(Spacing.md)
        } else {
            Modifier.padding(Spacing.md)
        },
    )
}
