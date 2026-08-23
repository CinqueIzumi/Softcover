package nl.rhaydus.softcover.feature.session.di

import org.koin.core.module.Module
import nl.rhaydus.softcover.core.presentation.session.ReadingSessionLauncher

/**
 * Platform-provided Koin binding the shared `sessionModule` pulls in via `includes(...)`. The one
 * piece that cannot be shared is the [ReadingSessionLauncher] implementation: Android starts a
 * foreground service, while platforms without that capability bind a no-op.
 */
expect val platformSessionModule: Module
