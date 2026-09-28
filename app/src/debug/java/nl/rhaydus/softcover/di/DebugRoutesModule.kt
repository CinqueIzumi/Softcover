package nl.rhaydus.softcover.di

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import nl.rhaydus.softcover.core.presentation.debug.DebugRoutesContent
import nl.rhaydus.softcover.debug.DebugRoutesScreenModel
import nl.rhaydus.softcover.debug.DebugRoutesSection
import nl.rhaydus.softcover.debug.MotionDebugScreenModel
import nl.rhaydus.softcover.debug.ShareCardDebugScreenModel
import org.koin.dsl.module

/**
 * Debug-build binding for the Settings debug routes — wires in the real [DebugRoutesSection]. The
 * release variant ([app/src/release]) binds a no-op, so the debug tooling never surfaces in release.
 */
internal val debugRoutesModule = module {
    single<DebugRoutesContent> {
        object : DebugRoutesContent {
            @Composable
            override fun Render(host: Screen) = DebugRoutesSection(host = host)
        }
    }

    factory { MotionDebugScreenModel(appDispatchers = get()) }
    factory { ShareCardDebugScreenModel(appDispatchers = get()) }
    factory { DebugRoutesScreenModel(appDispatchers = get()) }
}
