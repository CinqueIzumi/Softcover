package nl.rhaydus.softcover.core.component.snackbar

import androidx.compose.runtime.compositionLocalOf

/** How many modal windows sit above the app root, so a deeper window's host outranks a shallower one. */
internal val LocalSnackbarHostLevel = compositionLocalOf { 0 }
