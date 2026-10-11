package nl.rhaydus.softcover.core.component.snackbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.util.SnackBarManager

private const val SHORT_DURATION_MILLIS = 4_000L
private const val LONG_DURATION_MILLIS = 10_000L
private const val ENTER_SCALE = 0.8f

private val hostRegistry = SnackbarHostRegistry()
private val clockOrigin = TimeSource.Monotonic.markNow()

/**
 * The snackbar host bound to [SnackBarManager]. Place one at the app root; every
 * [nl.rhaydus.softcover.core.component.sheet.AdaptiveModalSheet] adds its own. Only the host with the
 * highest [LocalSnackbarHostLevel] draws, so a snackbar appears above the topmost window. When the
 * topmost host changes mid-display, the new one continues the same snackbar without an entrance and
 * counts down only the time that is left.
 */
@Composable
fun SoftcoverSnackbarHost(modifier: Modifier = Modifier) {
    val token = remember { Any() }
    val level = LocalSnackbarHostLevel.current

    DisposableEffect(token, level) {
        hostRegistry.register(
            token = token,
            level = level,
        )

        onDispose { hostRegistry.unregister(token) }
    }

    if (hostRegistry.isTopmost(token)) {
        val hostState by SnackBarManager.snackBarState.collectAsState()
        val data = hostState.currentSnackbarData

        SnackbarTimeout(data = data)

        val playMotion = playDecorativeMotion()
        val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

        AnimatedContent(
            targetState = data,
            modifier = modifier,
            transitionSpec = {
                if (playMotion) {
                    val enter = fadeIn(effectsSpec) + scaleIn(
                        animationSpec = spatialSpec,
                        initialScale = ENTER_SCALE,
                    )

                    enter togetherWith fadeOut(effectsSpec)
                } else {
                    ContentTransform(
                        targetContentEnter = EnterTransition.None,
                        initialContentExit = ExitTransition.None,
                    )
                }
            },
        ) { target ->
            if (target != null) {
                Snackbar(
                    snackbarData = target,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        dismiss {
                            target.dismiss()
                            true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SnackbarTimeout(data: SnackbarData?) {
    val accessibilityManager = LocalAccessibilityManager.current

    LaunchedEffect(data) {
        if (data == null) return@LaunchedEffect

        val hasAction = data.visuals.actionLabel != null
        val baseMillis = when (data.visuals.duration) {
            SnackbarDuration.Indefinite -> return@LaunchedEffect
            SnackbarDuration.Long -> LONG_DURATION_MILLIS
            SnackbarDuration.Short -> SHORT_DURATION_MILLIS
        }
        val durationMillis = accessibilityManager?.calculateRecommendedTimeoutMillis(
            originalTimeoutMillis = baseMillis,
            containsIcons = hasAction,
            containsText = true,
            containsControls = hasAction,
        ) ?: baseMillis

        delay(
            hostRegistry.remainingMillis(
                data = data,
                durationMillis = durationMillis,
                nowMillis = clockOrigin.elapsedNow().inWholeMilliseconds,
            ),
        )

        data.dismiss()
    }
}
