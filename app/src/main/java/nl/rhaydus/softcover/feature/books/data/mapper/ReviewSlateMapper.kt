package nl.rhaydus.softcover.feature.books.data.mapper

/**
 * Hardcover stores reviews as a Slate document — a list of block nodes, each carrying inline text
 * children. The app keeps the user's review as plain [String] body, so on publish we lift it into
 * the minimal Slate shape the API expects: one `paragraph` block per line, each holding a single
 * text node. Apollo serialises the resulting [List]/[Map] tree through its `Any` scalar adapter.
 */
fun reviewSlateFromBody(body: String): List<Map<String, Any?>> = body
    .split('\n')
    .map { line ->
        mapOf(
            "data" to emptyMap<String, Any?>(),
            "object" to "block",
            "type" to "paragraph",
            "children" to listOf(
                mapOf(
                    "object" to "text",
                    "text" to line,
                ),
            ),
        )
    }
