package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.presentation.navigation.TransientNavArg

/**
 * The full-screen cover viewer. It carries one already-mapped [CoverUiModel] rather than the book's
 * editions: resolving an edition into a cover is the book screen's collector's job (R9). The model holds
 * non-serializable image sources, so it is a [TransientNavArg]; after process-death restore it is null
 * and the viewer pops itself back to the book screen.
 */
// `internal`: the only thing that pushes this screen is `BookDetailScreen`, in this same module.
// Public, its `CoverUiModel` constructor argument would re-export `:core:component` from this
// feature's API surface, which `checkModuleGraph`'s api-visibility rule would (rightly) demand an
// allowlisted `api` edge for. Encapsulation is the better answer — the same call S3 made for
// `BookDetailPrefetcher`.
internal class FullScreenCoverScreen(
    @TransientNavArg private val cover: CoverUiModel?,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val model = cover

        if (model == null) {
            LaunchedEffect(Unit) {
                if (navigator.lastItem === this@FullScreenCoverScreen) navigator.pop()
            }

            return
        }

        FullScreenCoverScreenLayout(
            model = model,
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
    model: CoverUiModel,
    onNavigateUp: () -> Unit,
)
