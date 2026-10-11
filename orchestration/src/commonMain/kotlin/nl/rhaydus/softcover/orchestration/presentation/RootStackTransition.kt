package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Renders [navigator]'s top screen like Voyager's `ScreenTransition`, but owns the back gesture:
 * Voyager's own handler pops on release and cannot report progress, so the caller turns it off
 * (`onBackPressed = null`) and this drives a [SeekableTransitionState] from the gesture instead.
 * Where [backGestureSeeksTransition] is false the gesture just pops and the plain transition plays.
 */
@Composable
internal fun RootStackTransition(
    navigator: Navigator,
    transitionSpec: AnimatedContentTransitionScope<Screen>.(isPop: Boolean) -> ContentTransform,
    content: @Composable AnimatedVisibilityScope.(Screen) -> Unit,
) {
    val seekState = remember { SeekableTransitionState(navigator.lastItem) }

    var session by remember { mutableStateOf<BackGestureSession?>(null) }

    var committing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(navigator.lastItem) {
        session?.let { live ->
            if (live.from != navigator.lastItem) session = live.copy(abandoned = true)
        }

        seekState.animateTo(navigator.lastItem)
    }

    DisposableEffect(Unit) {
        onDispose { session = null }
    }

    val backState = rememberNavigationEventState(NavigationEventInfo.None)

    if (backGestureSeeksTransition) {
        LaunchedEffect(backState) {
            var idleSeen = false

            snapshotFlow { backState.transitionState }.collectLatest { transitionState ->
                if (transitionState !is NavigationEventTransitionState.InProgress) {
                    idleSeen = true

                    return@collectLatest
                }

                if (committing) return@collectLatest

                if (idleSeen) {
                    idleSeen = false

                    session = null
                }

                val live = session ?: run {
                    if (navigator.canPop.not()) return@collectLatest

                    BackGestureSession(
                        from = navigator.lastItem,
                        target = navigator.items[navigator.items.size - 2],
                    ).also { session = it }
                }

                if (live.abandoned) return@collectLatest

                if (live.from != navigator.lastItem) {
                    session = live.copy(abandoned = true)

                    return@collectLatest
                }

                seekState.seekTo(
                    fraction = transitionState.latestEvent.progress,
                    targetState = live.target,
                )
            }
        }
    }

    NavigationBackHandler(
        state = backState,
        isBackEnabled = navigator.canPop,
        onBackCancelled = {
            val live = session

            if (committing || live == null) return@NavigationBackHandler

            if (live.abandoned) {
                session = null

                return@NavigationBackHandler
            }

            scope.launch {
                try {
                    seekState.animateTo(live.from)
                } finally {
                    if (session === live && committing.not()) session = null
                }
            }
        },
        onBackCompleted = {
            val live = session

            when {
                committing -> Unit

                live == null -> if (navigator.canPop) navigator.pop()

                live.abandoned -> session = null

                else -> {
                    committing = true

                    scope.launch {
                        try {
                            seekState.animateTo(live.target)
                        } finally {
                            withContext(NonCancellable) {
                                if (navigator.lastItem == live.from && navigator.canPop) navigator.pop()
                            }

                            session = null

                            committing = false
                        }
                    }
                }
            }
        },
    )

    rememberTransition(seekState).AnimatedContent(
        transitionSpec = {
            transitionSpec(session?.abandoned == false || navigator.lastEvent == StackEvent.Pop)
        },
        contentKey = { it.key },
    ) { screen ->
        navigator.saveableState("transition", screen) {
            content(screen)
        }
    }
}
