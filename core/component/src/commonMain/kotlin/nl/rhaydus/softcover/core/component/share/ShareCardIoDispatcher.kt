package nl.rhaydus.softcover.core.component.share

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineDispatcher

/**
 * The dispatcher share-card captures run their file work on.
 */
val LocalShareCardIoDispatcher = staticCompositionLocalOf<CoroutineDispatcher> {
    error("LocalShareCardIoDispatcher is not provided; mount ProvideShareCardIoDispatcher at the composition root")
}
