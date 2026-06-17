package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement as ComposeWindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlin.math.roundToInt
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.WindowPlacement as DomainWindowPlacement
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDesktopWindowStateAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetDesktopWindowStateUseCase

// Writes are coalesced so a live drag-resize/move doesn't hammer the DataStore on every frame.
private const val WINDOW_STATE_SAVE_DEBOUNCE_MS = 400L

/**
 * A [WindowState] seeded from the persisted desktop window geometry and kept in sync back to it, so
 * the window's size, on-screen position, and placement survive a relaunch. The seed is read once
 * (a single small local-file read at window open) — no feedback loop, since the saver below only
 * writes. Size/position are only refreshed while the window is [ComposeWindowPlacement.Floating]; when
 * maximized or fullscreen the last floating bounds are kept and only the placement is recorded, so
 * un-maximizing restores to the prior floating geometry.
 */
@OptIn(FlowPreview::class)
@Composable
fun rememberPersistedWindowState(): WindowState {
    val getDesktopWindowState = koinInject<GetDesktopWindowStateAsFlowUseCase>()
    val setDesktopWindowState = koinInject<SetDesktopWindowStateUseCase>()

    val seed = remember { runBlocking { getDesktopWindowState().first() } }

    val windowState = rememberWindowState(
        placement = seed.placement.toCompose(),
        position = seed.toWindowPosition(),
        size = DpSize(
            seed.width.dp,
            seed.height.dp,
        ),
    )

    LaunchedEffect(windowState, setDesktopWindowState) {
        var lastFloating = seed.copy(placement = DomainWindowPlacement.FLOATING)

        snapshotFlow {
            Triple(
                windowState.size,
                windowState.position,
                windowState.placement,
            )
        }
            .debounce(WINDOW_STATE_SAVE_DEBOUNCE_MS)
            .collect { (size, position, placement) ->
                if (placement == ComposeWindowPlacement.Floating) {
                    val specified = position.isSpecified

                    lastFloating = DesktopWindowState(
                        width = size.width.value.roundToInt(),
                        height = size.height.value.roundToInt(),
                        x = if (specified) position.x.value.roundToInt() else null,
                        y = if (specified) position.y.value.roundToInt() else null,
                        placement = DomainWindowPlacement.FLOATING,
                    )
                    setDesktopWindowState(lastFloating)
                } else {
                    setDesktopWindowState(lastFloating.copy(placement = placement.toDomain()))
                }
            }
    }

    return windowState
}

private fun DesktopWindowState.toWindowPosition(): WindowPosition {
    val savedX = x
    val savedY = y

    return if (savedX != null && savedY != null) {
        WindowPosition(
            savedX.dp,
            savedY.dp,
        )
    } else {
        WindowPosition(Alignment.Center)
    }
}

private fun DomainWindowPlacement.toCompose(): ComposeWindowPlacement {
    return when (this) {
        DomainWindowPlacement.FLOATING -> ComposeWindowPlacement.Floating
        DomainWindowPlacement.MAXIMIZED -> ComposeWindowPlacement.Maximized
        DomainWindowPlacement.FULLSCREEN -> ComposeWindowPlacement.Fullscreen
    }
}

private fun ComposeWindowPlacement.toDomain(): DomainWindowPlacement {
    return when (this) {
        ComposeWindowPlacement.Floating -> DomainWindowPlacement.FLOATING
        ComposeWindowPlacement.Maximized -> DomainWindowPlacement.MAXIMIZED
        ComposeWindowPlacement.Fullscreen -> DomainWindowPlacement.FULLSCREEN
    }
}
