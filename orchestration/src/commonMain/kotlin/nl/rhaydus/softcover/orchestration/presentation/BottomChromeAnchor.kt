package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

/**
 * Where the compact bottom chrome starts, so the app-wide snackbar host — which sits outside the
 * navigator and cannot see the bar screen — can ride above it. [topInRoot] is `null` whenever no bottom
 * chrome is on screen (onboarding, rail/sidebar windows).
 */
@Stable
internal class BottomChromeAnchor {
    var topInRoot: Float? by mutableStateOf(null)
        private set

    private var owner: Any? = null

    fun report(
        owner: Any,
        top: Float,
    ) {
        this.owner = owner
        topInRoot = top
    }

    fun clear(owner: Any) {
        if (this.owner === owner) {
            this.owner = null
            topInRoot = null
        }
    }
}

internal val LocalBottomChromeAnchor = compositionLocalOf<BottomChromeAnchor?> { null }

/**
 * Reports the laid-out top edge of this node into the provided [BottomChromeAnchor]. Leaving composition
 * clears the anchor only while this node is still its reporter, so a replacement bar that reported first
 * is not wiped.
 */
@Composable
internal fun Modifier.reportBottomChromeTop(): Modifier {
    val anchor = LocalBottomChromeAnchor.current

    val owner = remember { Any() }

    DisposableEffect(anchor) {
        onDispose { anchor?.clear(owner) }
    }

    return onGloballyPositioned { coordinates ->
        anchor?.report(
            owner = owner,
            top = coordinates.positionInRoot().y,
        )
    }
}
