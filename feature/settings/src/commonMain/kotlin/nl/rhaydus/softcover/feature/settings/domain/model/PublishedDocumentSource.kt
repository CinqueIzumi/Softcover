package nl.rhaydus.softcover.feature.settings.domain.model

/**
 * Where a [PublishedDocument] came from, so the screen can tell the reader when it is showing an
 * offline copy.
 */
enum class PublishedDocumentSource {
    CACHE,
    BUNDLED,
}
