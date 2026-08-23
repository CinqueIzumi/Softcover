package nl.rhaydus.softcover.core.presentation.navigation

import androidx.compose.runtime.compositionLocalOf
import cafe.adriel.voyager.navigator.Navigator

/**
 * Cross-feature seam for opening a book's detail. A list surface (library, reading, explore) builds
 * a [ScreenDestination.BookDetail] and hands it to the presenter; the **app shell** decides *how* to
 * present it based on the window size — a full-screen push on compact/medium, or the trailing pane
 * of the list–detail two-pane on expanded. The feature stays unaware of which, so the same tap works
 * at every width with no functionality change.
 *
 * This complements [AppNavigator] (which resolves *what* `Screen` a destination is): the presenter
 * owns the push-vs-pane *decision* that only the shell can make.
 */
fun interface BookDetailPresenter {
    fun open(destination: ScreenDestination.BookDetail)
}

/**
 * The shell-provided [BookDetailPresenter]. `null` when no shell is present (previews, tests) — call
 * sites fall back to their own push so they remain self-contained.
 */
val LocalBookDetailPresenter = compositionLocalOf<BookDetailPresenter?> { null }

/**
 * Provided by the two-pane detail host so the book-detail surface's back affordance clears the pane
 * instead of popping a navigator. `null` on the normal pushed path, where back pops as usual.
 */
val LocalBookDetailPaneCloseHandler = compositionLocalOf<(() -> Unit)?> { null }

/**
 * The navigator that full-screen overlays opened *from* the book-detail surface (the zoomable cover
 * viewer, the create-list flow) should be pushed onto. In the two-pane layout the detail lives in a
 * nested navigator, so the pane host provides the **root** navigator here to keep those overlays
 * full-screen instead of cropped into the pane. `null` on the pushed path — overlays use the
 * detail's own navigator, exactly as before.
 */
val LocalBookDetailOverlayNavigator = compositionLocalOf<Navigator?> { null }
