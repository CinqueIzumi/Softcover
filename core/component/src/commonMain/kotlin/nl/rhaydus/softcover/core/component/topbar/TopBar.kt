package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/** The scrim a control needs to stay legible on [TopBarSurface.OVER_MEDIA]. */
private val OVER_MEDIA_SCRIM = Color.Black.copy(alpha = 0.35f)

/**
 * The app's page bar: a centred, autosizing title with an optional subtitle, an optional back
 * affordance, and a trailing slot for the screen's own actions.
 *
 * [scrollBehavior] is Compose plumbing rather than model data — it is a hoisted, mutable state
 * object a `Scaffold` and its content share, so it travels as a parameter for the same reason
 * [modifier] does.
 *
 * [actions] receives the [IconButtonColors] the bar's own controls are using, so a screen's action —
 * the book page's overflow menu is the one that needs it — takes the same scrim treatment as the
 * back arrow without re-deriving it. A screen with no actions ignores the parameter.
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
    val containerColor by animateColorAsState(
        targetValue = when (model.surface) {
            TopBarSurface.OPAQUE -> Color.Unspecified
            TopBarSurface.OVER_MEDIA -> Color.Transparent
        },
        label = "TopBarContainer",
    )

    val controlColors = when (model.surface) {
        TopBarSurface.OPAQUE -> IconButtonDefaults.iconButtonColors()

        TopBarSurface.OVER_MEDIA -> IconButtonDefaults.iconButtonColors(
            containerColor = OVER_MEDIA_SCRIM,
            contentColor = Color.White,
        )
    }

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = model.title,
                autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.titleLarge.fontSize),
                maxLines = 2,
            )
        },
        subtitle = { model.subtitle?.let { subtitle -> Text(text = subtitle) } },
        scrollBehavior = scrollBehavior,
        titleHorizontalAlignment = Alignment.CenterHorizontally,
        colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = containerColor),
        actions = { actions(controlColors) },
        navigationIcon = {
            when (model.navigation) {
                TopBarNavigation.None -> Unit

                TopBarNavigation.Back -> IconButton(
                    onClick = { onEvent(TopBarEvent.BackClicked) },
                    colors = controlColors,
                ) {
                    val icon = drawableIconResource(
                        icon = SoftcoverIcon.ArrowBack,
                        contentDescription = "Navigate back icon",
                    )

                    Icon(
                        painter = icon.getIconPainter(),
                        contentDescription = icon.contentDescription,
                    )
                }
            }
        },
    )
}
