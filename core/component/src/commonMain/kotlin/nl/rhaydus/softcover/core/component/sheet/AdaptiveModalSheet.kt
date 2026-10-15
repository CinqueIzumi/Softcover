package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.softcover.core.component.snackbar.LocalSnackbarHostLevel
import nl.rhaydus.softcover.core.component.snackbar.SoftcoverSnackbarHost
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

private const val PANEL_ENTER_SCALE = 0.92f

/** True from the first close path (scrim, back, swipe, a body's dismiss or a host's close) until the sheet is gone. */
internal val LocalSheetClosing = compositionLocalOf { false }

/**
 * The height, in pixels, the bottom sheet's snackbar keeps clear at the bottom: a [SheetScaffold] footer
 * reports it so the snackbar floats above the buttons. Null outside a bottom sheet.
 */
internal val LocalSheetFooterInset = compositionLocalOf<MutableIntState?> { null }

/**
 * A modal surface that is a bottom sheet below expanded widths and an animated, elevated panel at
 * expanded widths.
 *
 * Every close path (outside tap, back, swipe on the bottom sheet, Esc on the panel and
 * [LocalModalSheetDismiss]) plays the exit animation first and calls [onDismissRequest] once, after it
 * finishes. The host must remove the sheet in response; neither form resets itself. Outside tap and swipe
 * are refused while [dismissOnTapOutside] is false, and back while [dismissOnBackPress] is false. A
 * [LocalModalSheetDismiss] hide still plays when both are refused.
 *
 * Each sheet window hosts the app snackbar, so callers never add their own. On the bottom sheet it floats
 * over the content, above a [SheetScaffold] footer, so the sheet never resizes for it.
 */
@Composable
fun AdaptiveModalSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnTapOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val windowSize = rememberWindowSizeClass()
    val sheetLevel = LocalSnackbarHostLevel.current + 1

    if (windowSize.widthClass == WindowWidthClass.EXPANDED) {
        AnimatedSheetPanel(
            onDismissRequest = onDismissRequest,
            maxHeight = windowSize.heightDp * 0.9f,
            sheetLevel = sheetLevel,
            dismissOnTapOutside = dismissOnTapOutside,
            dismissOnBackPress = dismissOnBackPress,
            modifier = modifier,
            content = content,
        )
    } else {
        BottomSheetForm(
            onDismissRequest = onDismissRequest,
            sheetLevel = sheetLevel,
            dismissOnTapOutside = dismissOnTapOutside,
            dismissOnBackPress = dismissOnBackPress,
            modifier = modifier,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetForm(
    onDismissRequest: () -> Unit,
    sheetLevel: Int,
    dismissOnTapOutside: Boolean,
    dismissOnBackPress: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    var closing by remember { mutableStateOf(false) }
    var dismissSent by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val footerInset = remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target ->
            if (target == SheetValue.Hidden) {
                closing = true
            }

            true
        },
    )
    val notifyDismissed: () -> Unit = remember {
        {
            if (dismissSent.not()) {
                dismissSent = true
                currentOnDismissRequest()
            }
        }
    }
    val dismiss: () -> Unit = remember(sheetState, coroutineScope) {
        {
            if (closing.not()) {
                closing = true

                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { notifyDismissed() }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = notifyDismissed,
        modifier = modifier,
        sheetState = sheetState,
        sheetGesturesEnabled = dismissOnTapOutside,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = dismissOnBackPress,
            shouldDismissOnClickOutside = dismissOnTapOutside,
        ),
    ) {
        CompositionLocalProvider(
            LocalModalSheetForm provides ModalSheetForm.SHEET,
            LocalSnackbarHostLevel provides sheetLevel,
            LocalSheetClosing provides closing,
            LocalModalSheetDismiss provides dismiss,
            LocalSheetFooterInset provides footerInset,
        ) {
            Box {
                Column(content = content)

                SheetSnackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = -footerInset.intValue,
                            )
                        },
                    insetNavigationBars = false,
                )
            }
        }
    }
}

@Composable
private fun AnimatedSheetPanel(
    onDismissRequest: () -> Unit,
    maxHeight: Dp,
    sheetLevel: Int,
    dismissOnTapOutside: Boolean,
    dismissOnBackPress: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    var closing by remember { mutableStateOf(false) }
    val scale = remember { Animatable(initialValue = PANEL_ENTER_SCALE) }
    val alpha = remember { Animatable(initialValue = 0f) }
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
    val close: () -> Unit = remember { { closing = true } }

    LaunchedEffect(closing) {
        coroutineScope {
            launch {
                scale.animateTo(
                    targetValue = if (closing) PANEL_ENTER_SCALE else 1f,
                    animationSpec = spatialSpec,
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = if (closing) 0f else 1f,
                    animationSpec = effectsSpec,
                )
            }
        }

        if (closing) currentOnDismissRequest()
    }

    CompositionLocalProvider(LocalSnackbarHostLevel provides sheetLevel) {
        ScrimlessDialog(
            onDismissRequest = close,
            dismissOnBackPress = dismissOnBackPress,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { this.alpha = alpha.value }
                        .background(scrimColor)
                        .pointerInput(dismissOnTapOutside) {
                            if (dismissOnTapOutside) detectTapGestures { close() }
                        },
                )

                Box(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = modifier
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                                this.alpha = alpha.value
                            }
                            .widthIn(max = 420.dp)
                            .fillMaxWidth()
                            .heightIn(max = maxHeight),
                        shape = Corners.xxl,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shadowElevation = 6.dp,
                    ) {
                        Column(modifier = Modifier.padding(top = Spacing.md)) {
                            CompositionLocalProvider(
                                LocalModalSheetForm provides ModalSheetForm.PANEL,
                                LocalModalSheetDismiss provides close,
                                LocalSheetClosing provides closing,
                            ) {
                                content()
                            }
                        }
                    }
                }

                SheetSnackbar(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    insetNavigationBars = true,
                )
            }
        }
    }
}

/** The panel sits under the system bars, so it adds the navigation-bar inset; the bottom sheet already applies it. */
@Composable
private fun SheetSnackbar(
    modifier: Modifier,
    insetNavigationBars: Boolean,
) {
    SoftcoverSnackbarHost(
        modifier = modifier.widthIn(max = 420.dp),
        snackbarModifier = if (insetNavigationBars) {
            Modifier
                .navigationBarsPadding()
                .padding(Spacing.md)
        } else {
            Modifier.padding(Spacing.md)
        },
    )
}
