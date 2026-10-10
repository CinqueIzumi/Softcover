package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.chrome.overMediaIconButtonColors
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * The app's page bar: a centred, autosizing title, a back button, and a trailing slot for the
 * screen's own actions.
 *
 * [scrollBehavior] is Compose plumbing rather than model data — it is a hoisted, mutable state
 * object a `Scaffold` and its content share, so it travels as a parameter for the same reason
 * [modifier] does.
 *
 * [actions] receives the [IconButtonColors] the bar's own controls are using, so a screen's action
 * takes the same scrim treatment as the back arrow without re-deriving it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopBar(
    model: TopBarUiModel,
    onEvent: (TopBarEvent) -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.(IconButtonColors) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = when (model.surface) {
            TopBarSurface.Opaque -> scheme.surface
            TopBarSurface.OverMedia -> Color.Transparent
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "TopBarContainer",
    )

    val controlColors = when (model.surface) {
        TopBarSurface.Opaque -> IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = scheme.onSurface,
        )

        TopBarSurface.OverMedia -> overMediaIconButtonColors()
    }

    TopAppBar(
        modifier = modifier,
        title = {
            model.title?.let { title ->
                SoftcoverText(
                    text = title,
                    modifier = Modifier.semantics { heading() },
                    autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.titleLarge.fontSize),
                    maxLines = 2,
                )
            }
        },
        subtitle = {},
        scrollBehavior = scrollBehavior,
        titleHorizontalAlignment = Alignment.CenterHorizontally,
        colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = containerColor),
        actions = { actions(controlColors) },
        navigationIcon = {
            ChromeIconButton(
                icon = SoftcoverIcon.ArrowBack,
                contentDescription = "Navigate back",
                tooltip = "Back",
                onClick = { onEvent(TopBarEvent.BackClicked) },
                colors = controlColors,
            )
        },
    )
}
