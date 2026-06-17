package nl.rhaydus.softcover.core.database.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin binding the shared `databaseModule` pulls in via `includes(...)`. Each
 * target supplies the one piece that cannot be shared: the `SoftcoverDatabase` builder, whose
 * on-disk location (and, on Android, the `Context`) is platform-bound. The migrations, bundled
 * SQLite driver, and query context are shared via `SoftcoverDatabase.build`.
 */
expect val platformDatabaseModule: Module
