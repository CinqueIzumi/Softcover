package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import nl.rhaydus.designsystem.motion.playDecorativeMotion

private const val PUSH_SCALE_FROM = 0.92f

/**
 * Hosts a [Navigator] rooted at [screen] and renders its top screen like Voyager's `ScreenTransition`,
 * but owns the back gesture: Voyager's own handler pops on release and cannot report progress, so the
 * navigator is created without it (`onBackPressed = null`) and a [SeekableTransitionState] is driven
 * from the gesture instead. Where [backGestureSeeksTransition] is false, or the transition has no
 * duration (reduced motion, read from `playDecorativeMotion()`), the gesture just pops and the plain
 * transition plays.
 *
 * A back press that lands while the commit animation is still running is swallowed: the pop it
 * would trigger is already under way.
 *
 * [navigatorEffect] runs for as long as the navigator lives.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RootStackTransition(
    screen: Screen,
    navigatorEffect: suspend (Navigator) -> Unit,
    content: @Composable AnimatedVisibilityScope.(Screen) -> Unit,
) {
    Navigator(
        screen = screen,
        onBackPressed = null,
    ) { navigator ->
        LaunchedEffect(navigator) {
            navigatorEffect(navigator)
        }

        val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

        val seekState = remember { SeekableTransitionState(navigator.lastItem) }

        val playMotion by rememberUpdatedState(playDecorativeMotion())

        var session by remember { mutableStateOf<BackGestureSession?>(null) }

        val scope = rememberCoroutineScope()

        LaunchedEffect(navigator.lastItem) {
            session = session?.following(navigator.lastItem)

            seekState.animateTo(navigator.lastItem)
        }

        val backState = rememberNavigationEventState(NavigationEventInfo.None)

        if (backGestureSeeksTransition) {
            LaunchedEffect(backState) {
                snapshotFlow { backState.transitionState }.collectLatest { transitionState ->
                    if (transitionState !is NavigationEventTransitionState.InProgress) {
                        session = session?.released()

                        return@collectLatest
                    }

                    val current = session

                    if (current is BackGestureSession.Committing) return@collectLatest

                    val live = current?.takeUnless { it.isReleased } ?: run {
                        if (navigator.canPop.not() || playMotion.not()) {
                            return@collectLatest
                        }

                        BackGestureSession.Seeking(
                            from = navigator.lastItem,
                            target = navigator.items[navigator.items.size - 2],
                        )
                    }

                    val next = live.following(navigator.lastItem)

                    session = next

                    if (next is BackGestureSession.Seeking) {
                        seekState.seekTo(
                            fraction = transitionState.latestEvent.progress,
                            targetState = next.target,
                        )
                    }
                }
            }
        }

        NavigationBackHandler(
            state = backState,
            isBackEnabled = navigator.canPop,
            onBackCancelled = {
                when (val live = session) {
                    is BackGestureSession.Seeking -> scope.launch {
                        try {
                            seekState.animateTo(live.from)
                        } finally {
                            if (session?.isReleased == true) session = null
                        }
                    }

                    is BackGestureSession.Abandoned -> session = null

                    is BackGestureSession.Committing,
                    null -> Unit
                }
            },
            onBackCompleted = {
                when (val live = session) {
                    is BackGestureSession.Seeking -> {
                        val committing = live.commit()

                        session = committing

                        scope.launch {
                            try {
                                seekState.animateTo(committing.target)
                            } finally {
                                withContext(NonCancellable) {
                                    if (navigator.lastItem == committing.from && navigator.canPop) {
                                        navigator.pop()
                                    }
                                }

                                session = null
                            }
                        }
                    }

                    is BackGestureSession.Abandoned -> session = null

                    is BackGestureSession.Committing -> Unit

                    null -> if (navigator.canPop) navigator.pop()
                }
            },
        )

        rememberTransition(seekState).AnimatedContent(
            transitionSpec = {
                val liveGesture = session.let { it != null && it !is BackGestureSession.Abandoned }

                rootFadeThrough(
                    isPop = liveGesture || navigator.lastEvent == StackEvent.Pop,
                    effectsSpec = effectsSpec,
                    spatialSpec = spatialSpec,
                )
            },
            contentKey = { it.key },
        ) { current ->
            navigator.saveableState("transition", current) {
                content(current)
            }
        }
    }
}

private fun rootFadeThrough(
    isPop: Boolean,
    effectsSpec: FiniteAnimationSpec<Float>,
    spatialSpec: FiniteAnimationSpec<Float>,
): ContentTransform = if (isPop) {
    val shrinkOut = scaleOut(
        animationSpec = spatialSpec,
        targetScale = PUSH_SCALE_FROM,
    )

    navFadeThrough(
        effectsSpec = effectsSpec,
        exitMotion = shrinkOut,
    ).apply { targetContentZIndex = -1f }
} else {
    val growIn = scaleIn(
        animationSpec = spatialSpec,
        initialScale = PUSH_SCALE_FROM,
    )

    navFadeThrough(
        effectsSpec = effectsSpec,
        enterMotion = growIn,
    )
}
