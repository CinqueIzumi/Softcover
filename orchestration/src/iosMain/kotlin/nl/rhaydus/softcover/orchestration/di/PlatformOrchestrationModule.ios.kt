package nl.rhaydus.softcover.orchestration.di

import org.koin.core.module.Module
import org.koin.dsl.module

// iOS has no launcher Activity, and the AppEntryPoint seam (an Android Intent builder) does not exist
// on this platform — its only consumer, the reading-session foreground service, is Android-only. So
// there is nothing to bind here; the module is intentionally empty.
internal actual val platformOrchestrationModule: Module = module { }
