package nl.rhaydus.softcover.orchestration.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin binding the shared [orchestrationModule] pulls in via `includes(...)`. The
 * one piece that cannot be shared is the `AppEntryPoint` implementation — it builds an Android
 * launcher `Intent` targeting the concrete `MainActivity`, so only Android binds it (its sole caller,
 * `feature:session`'s reading-session service, is also Android-only). Platforms without a launcher
 * Activity bind nothing.
 */
internal expect val platformOrchestrationModule: Module
