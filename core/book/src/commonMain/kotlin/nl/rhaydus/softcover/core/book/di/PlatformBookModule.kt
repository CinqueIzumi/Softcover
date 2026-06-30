package nl.rhaydus.softcover.core.book.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin bindings the shared `bookModule` pulls in via `includes(...)`. Each target
 * supplies the one piece that cannot be shared: the `EditionImageStorage` root directory location
 * (Android: `filesDir`; iOS: the documents directory).
 */
expect val platformBookModule: Module
