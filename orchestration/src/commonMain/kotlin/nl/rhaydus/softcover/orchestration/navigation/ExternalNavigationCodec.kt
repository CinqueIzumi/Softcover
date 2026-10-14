package nl.rhaydus.softcover.orchestration.navigation

import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination

private const val PREFIX = "softcover://open/"

// Wire literals are persisted in PendingIntents that outlive app updates; never derive them from enum names.
internal fun ExternalNavigationRequest.toUriString(): String =
    PREFIX + when (this) {
        is ExternalNavigationRequest.OpenTab -> "tab/${destination.wireName()}"
        is ExternalNavigationRequest.OpenBook -> "book/$bookId"
        ExternalNavigationRequest.OpenFocusMode -> "focus"
    }

internal fun parseExternalNavigationRequest(uri: String): ExternalNavigationRequest? {
    if (uri.startsWith(PREFIX).not()) return null

    val segments = uri.removePrefix(PREFIX).split("/")

    return when (segments.first()) {
        "tab" -> segments.takeIf { it.size == 2 }
            ?.let { tabFromWireName(it[1]) }
            ?.let { ExternalNavigationRequest.OpenTab(destination = it) }

        "book" -> segments.takeIf { it.size == 2 }
            ?.let { it[1].toIntOrNull() }
            ?.let { ExternalNavigationRequest.OpenBook(bookId = it) }

        "focus" -> ExternalNavigationRequest.OpenFocusMode.takeIf { segments.size == 1 }

        else -> null
    }
}

private fun TabDestination.wireName(): String =
    when (this) {
        TabDestination.READING -> "reading"
        TabDestination.LIBRARY -> "library"
        TabDestination.EXPLORE -> "explore"
        TabDestination.SETTINGS -> "settings"
    }

private fun tabFromWireName(name: String): TabDestination? =
    when (name) {
        "reading" -> TabDestination.READING
        "library" -> TabDestination.LIBRARY
        "explore" -> TabDestination.EXPLORE
        "settings" -> TabDestination.SETTINGS
        else -> null
    }
