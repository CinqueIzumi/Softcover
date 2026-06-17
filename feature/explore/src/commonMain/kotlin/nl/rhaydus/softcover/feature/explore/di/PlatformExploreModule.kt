package nl.rhaydus.softcover.feature.explore.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin bindings the shared `exploreModule` pulls in via `includes(...)`. Each
 * target supplies the one piece that cannot be shared: the `SearchHistoryDataStore` file location.
 */
expect val platformExploreModule: Module
