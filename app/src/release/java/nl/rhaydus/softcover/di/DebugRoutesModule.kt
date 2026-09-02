package nl.rhaydus.softcover.di

import androidx.compose.runtime.Composable
import org.koin.dsl.module
import nl.rhaydus.softcover.core.presentation.debug.DebugRoutesContent

/**
 * Release-build binding for the Settings debug routes — a no-op, so the debug tooling never surfaces
 * in release. The debug variant (`app/src/debug`) wires the real
 * [nl.rhaydus.softcover.debug.DebugRoutesSection].
 *
 * The screens themselves live in `app/src/debug` on `debugImplementation`, so in a release build
 * this binding is not hiding them — they are not in the binary at all.
 */
internal val debugRoutesModule = module {
    single<DebugRoutesContent> {
        object : DebugRoutesContent {
            @Composable
            override fun Render() = Unit
        }
    }
}
