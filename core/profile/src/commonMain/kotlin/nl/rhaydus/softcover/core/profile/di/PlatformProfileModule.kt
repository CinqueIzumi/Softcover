package nl.rhaydus.softcover.core.profile.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin bindings the shared `profileModule` pulls in via `includes(...)`. Each
 * target supplies the one piece that cannot be shared: the `ProfileCacheDataStore` file location.
 */
expect val platformProfileModule: Module
