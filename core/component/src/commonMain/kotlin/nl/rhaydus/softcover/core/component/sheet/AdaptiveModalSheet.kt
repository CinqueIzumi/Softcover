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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.component.AdaptiveModalSheet as FoundationAdaptiveModalSheet
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

/**
 * A modal surface that is a bottom sheet below expanded widths and an animated, elevated panel at
 * expanded widths. Below expanded it delegates to the foundation's `AdaptiveModalSheet` unchanged.
 *
 * Every close path of the panel (outside tap, back, Esc and [LocalModalSheetDismiss]) plays the
 * exit animation first and calls [onDismissRequest] once, after it finishes. The host must remove the
 * sheet in response; the panel does not reset itself, as with the foundation bottom sheet.
 *
 * Each sheet window hosts the app snackbar, so callers never add their own.
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
        FoundationAdaptiveModalSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            dismissOnTapOutside = dismissOnTapOutside,
            dismissOnBackPress = dismissOnBackPress,
        ) {
            CompositionLocalProvider(LocalSnackbarHostLevel provides sheetLevel) {
                Box {
                    Column { content() }

                    SoftcoverSnackbarHost(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(Spacing.md),
                    )
                }
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
                            ) {
                                content()
                            }
                        }
                    }
                }

                SoftcoverSnackbarHost(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(Spacing.md)
                        .widthIn(max = 420.dp),
                )
            }
        }
    }
}
