package nl.rhaydus.softcover.di

import androidx.compose.runtime.Composable
import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesContent
import nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesSection

/**
 * Debug-build binding for the Settings debug routes — wires in the real [DebugRoutesSection]. The
 * release variant ([app/src/release]) binds a no-op, so the debug tooling never surfaces in release.
 */
internal val debugRoutesModule = module {
    single<DebugRoutesContent> {
        object : DebugRoutesContent {
            @Composable
            override fun Render() = DebugRoutesSection()
        }
    }
}
