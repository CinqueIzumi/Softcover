package nl.rhaydus.softcover.feature.settings.data.mapper

import nl.rhaydus.softcover.core.database.model.PublishedDocumentEntity
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource

internal fun PublishedDocumentEntity.toPublishedDocument(): PublishedDocument = PublishedDocument(
    blocks = markdown.toMarkdownBlocks(),
    fetchedAtEpochMillis = fetchedAtEpochMillis,
    source = PublishedDocumentSource.CACHE,
)

internal fun String.toBundledPublishedDocument(): PublishedDocument = PublishedDocument(
    blocks = toMarkdownBlocks(),
    fetchedAtEpochMillis = null,
    source = PublishedDocumentSource.BUNDLED,
)
