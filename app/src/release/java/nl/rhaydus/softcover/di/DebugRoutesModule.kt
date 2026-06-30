package nl.rhaydus.softcover.di

import androidx.compose.runtime.Composable
import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesContent

/**
 * Release-build binding for the Settings debug routes — a no-op, so the debug tooling compiled into
 * the design system never surfaces in release. The debug variant ([app/src/debug]) wires the real
 * [nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesSection].
 */
internal val debugRoutesModule = module {
    single<DebugRoutesContent> {
        object : DebugRoutesContent {
            @Composable
            override fun Render() = Unit
        }
    }
}
