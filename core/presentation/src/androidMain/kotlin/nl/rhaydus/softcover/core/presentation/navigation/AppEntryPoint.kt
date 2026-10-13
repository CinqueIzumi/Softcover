package nl.rhaydus.softcover.core.presentation.navigation

import android.content.Context
import android.content.Intent

/**
 * Builds intents that target the app's launcher Activity. Implemented in the orchestration tier
 * (which owns the Activity), so a feature can deep-link into the app — e.g. from a notification —
 * without referencing the concrete Activity class (an upward dependency the module split rejects).
 */
interface AppEntryPoint {
    /** Intent that brings the app to the foreground and opens Focus Mode. */
    fun focusModeIntent(context: Context): Intent
}
