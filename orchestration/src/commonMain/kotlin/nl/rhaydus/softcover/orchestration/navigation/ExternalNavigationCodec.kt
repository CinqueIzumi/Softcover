package nl.rhaydus.softcover.orchestration.navigation

import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination

private const val PREFIX = "softcover://open/"

// Wire literals are persisted in PendingIntents that outlive app updates; never derive them from enum names.
internal fun ExternalNavigationRequest.toUriString(): String =
    PREFIX + when (this) {
        is ExternalNavigationRequest.OpenTab -> "tab/${destination.wireName()}"
        is ExternalNavigationRequest.OpenBook -> "book/$bookId"
        is ExternalNavigationRequest.OpenBookVerdict -> "book/$bookId/verdict"
        ExternalNavigationRequest.OpenFocusMode -> "focus"
    }

internal fun parseExternalNavigationRequest(uri: String): ExternalNavigationRequest? {
    if (uri.startsWith(PREFIX).not()) return null

    val segments = uri.removePrefix(PREFIX).split("/")

    return when (segments.first()) {
        "tab" -> segments.takeIf { it.size == 2 }
            ?.let { tabFromWireName(it[1]) }
            ?.let { ExternalNavigationRequest.OpenTab(destination = it) }

        "book" -> parseBook(segments)

        "focus" -> ExternalNavigationRequest.OpenFocusMode.takeIf { segments.size == 1 }

        else -> null
    }
}

private fun parseBook(segments: List<String>): ExternalNavigationRequest? {
    val bookId = segments.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 } ?: return null

    return when {
        segments.size == 2 -> ExternalNavigationRequest.OpenBook(bookId = bookId)

        segments.size == 3 && segments[2] == "verdict" -> ExternalNavigationRequest.OpenBookVerdict(bookId = bookId)

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
