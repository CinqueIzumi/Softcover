package nl.rhaydus.softcover.orchestration.di

import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.core.presentation.widget.UnsupportedHomeScreenWidgets
import org.koin.core.module.Module
import org.koin.dsl.module

// iOS has no launcher Activity, and the AppEntryPoint seam (an Android Intent builder) does not exist
// on this platform — its only consumer, the reading-session foreground service, is Android-only. The
// only binding is the "no home-screen widgets" stand-in that keeps Settings from offering a Widgets page.
internal actual val platformOrchestrationModule: Module = module {
    single<HomeScreenWidgets> { UnsupportedHomeScreenWidgets }
}
