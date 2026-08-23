package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.request.ImageRequest
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberEditionImageRequest
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.navigation.TransientNavArg

class FullScreenCoverScreen(
    @TransientNavArg private val edition: BookEdition?,
    @TransientNavArg private val defaultEdition: BookEdition?,
    private val fallbackCoverUrl: String?,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val request = rememberEditionImageRequest(
            edition = edition,
            defaultEdition = defaultEdition,
            fallbackCoverUrl = fallbackCoverUrl,
        )

        FullScreenCoverScreenLayout(
            request = request,
            onNavigateUp = navigator::pop,
        )
    }
}

// The mobile actual zooms/pans with touch (pinch + double-tap); the desktop actual uses mouse wheel
// zoom + drag-pan + double-click. Both render the shared [FullScreenCoverViewer] over their own
// zoom/pan state. No default arguments — they are not allowed on an expect declaration, so every
// argument is supplied explicitly at the single call site above.
@Composable
internal expect fun FullScreenCoverScreenLayout(
    request: ImageRequest?,
    onNavigateUp: () -> Unit,
)
