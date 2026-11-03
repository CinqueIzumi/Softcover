package nl.rhaydus.softcover.core.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.component.share.LocalShareCardIoDispatcher
import org.koin.compose.koinInject

/**
 * Supplies [LocalShareCardIoDispatcher] with the app's IO dispatcher.
 */
@Composable
fun ProvideShareCardIoDispatcher(content: @Composable () -> Unit) {
    val dispatchers = koinInject<AppDispatchers>()

    CompositionLocalProvider(
        LocalShareCardIoDispatcher provides dispatchers.io,
        content = content,
    )
}
