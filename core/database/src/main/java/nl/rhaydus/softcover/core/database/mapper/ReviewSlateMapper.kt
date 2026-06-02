package nl.rhaydus.softcover.core.database.mapper

import kotlinx.serialization.json.Json
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.canonical

private val reviewJson: Json = Json { ignoreUnknownKeys = true }

/**
 * Lift a [ReviewDocument] into the Slate shape the API expects for the `review_slate` input: one
 * `paragraph` block per paragraph, each holding its inline text nodes. Mark keys are emitted only when
 * `true`, matching how Hardcover's own client serialises them (false flags are omitted). Apollo writes
 * the resulting [List]/[Map] tree through its `Any` scalar adapter. An empty paragraph still emits a
 * single empty text node so the block stays well-formed.
 */
fun reviewSlateFromDocument(document: ReviewDocument): List<Map<String, Any?>> =
    document.paragraphs.map { paragraph ->
        val children: List<Map<String, Any?>> = paragraph.runs
            .ifEmpty { listOf(ReviewRun(text = "")) }
            .map { run ->
                buildMap {
                    put("object", "text")
                    put("text", run.text)

                    if (run.bold) put("bold", true)

                    if (run.italic) put("italic", true)

                    if (run.spoiler) put("spoiler", true)
                }
            }

        mapOf(
            "data" to emptyMap<String, Any?>(),
            "type" to "paragraph",
            "object" to "block",
            "children" to children,
        )
    }

/**
 * Parse the `review_slate` value into a [ReviewDocument]. The field arrives as Apollo's default `jsonb`
 * shape — nested [List]/[Map] of primitives — and the shape is asymmetric: what we *write* is a bare
 * list of paragraph blocks, but what the API *returns* on read is wrapped as
 * `{"document": {"children": [ ...blocks ]}}`. This accepts both — a bare list, a `{children: [...]}`
 * map, or the wrapped `{document: {children: [...]}}` map — and defends against missing/unexpected keys.
 */
fun reviewDocumentFromSlate(slate: Any?): ReviewDocument {
    val blocks: List<*> = blocksFrom(slate = slate) ?: return ReviewDocument.EMPTY

    val paragraphs: List<ReviewParagraph> = blocks.mapNotNull { block ->
        val map = block as? Map<*, *> ?: return@mapNotNull null

        val children = map["children"] as? List<*> ?: emptyList<Any?>()

        ReviewParagraph(runs = runsFrom(children = children))
    }

    return ReviewDocument(paragraphs = paragraphs).canonical()
}

/** Resolve the list of paragraph blocks from any of the slate shapes the API uses. */
private fun blocksFrom(slate: Any?): List<*>? = when (slate) {
    is List<*> -> slate

    is Map<*, *> -> {
        val document = slate["document"] as? Map<*, *>

        val children = (document ?: slate)["children"] as? List<*>

        children
    }

    else -> null
}

private fun runsFrom(children: List<*>): List<ReviewRun> =
    children.flatMap { child ->
        val map = child as? Map<*, *> ?: return@flatMap emptyList()

        val text = map["text"] as? String

        if (text != null) {
            listOf(
                ReviewRun(
                    text = text,
                    bold = map["bold"] as? Boolean ?: false,
                    italic = map["italic"] as? Boolean ?: false,
                    spoiler = map["spoiler"] as? Boolean ?: false,
                ),
            )
        } else {
            val nested = map["children"] as? List<*> ?: emptyList<Any?>()

            runsFrom(children = nested)
        }
    }

/** Serialise a [ReviewDocument] for durable storage (Room cache, offline write queue). */
fun ReviewDocument.toJson(): String = reviewJson.encodeToString(ReviewDocument.serializer(), this)

/** Restore a [ReviewDocument] persisted via [toJson]; `null` when the stored JSON is absent or invalid. */
fun reviewDocumentFromJson(json: String?): ReviewDocument? = json
    ?.let { runCatching { reviewJson.decodeFromString(ReviewDocument.serializer(), it) }.getOrNull() }
