package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

/**
 * The full-screen cover viewer. It carries one already-mapped [CoverUiModel] rather than the book's
 * editions: resolving an edition into a cover is the book screen's collector's job (R9), and a UI
 * model is a small immutable value, so unlike a domain graph it needs no `TransientNavArg`.
 */
// `internal`: the only thing that pushes this screen is `BookDetailScreen`, in this same module.
// Public, its `CoverUiModel` constructor argument would re-export `:core:component` from this
// feature's API surface, which `checkModuleGraph`'s api-visibility rule would (rightly) demand an
// allowlisted `api` edge for. Encapsulation is the better answer — the same call S3 made for
// `BookDetailPrefetcher`.
internal class FullScreenCoverScreen(
    private val cover: CoverUiModel,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        FullScreenCoverScreenLayout(
            model = cover,
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
