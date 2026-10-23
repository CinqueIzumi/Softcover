package nl.rhaydus.softcover.showcase.render

private val camelBoundary = Regex(pattern = "(?<=[a-z0-9])(?=[A-Z])")
private val nonAlphanumeric = Regex(pattern = "[^a-z0-9]+")

/** Lowercase-kebab form of [this], safe as a file or directory name. Blank input yields [fallback]. */
internal fun String.toSlug(fallback: String): String = replace(
    regex = camelBoundary,
    replacement = "-",
)
    .lowercase()
    .replace(
        regex = nonAlphanumeric,
        replacement = "-",
    )
    .trim('-')
    .ifEmpty { fallback }

/**
 * Slugs for [labels] that are unique within the list: a repeat gets `-2`, `-3`, and so on, skipping any
 * suffixed slug another label already claims.
 */
internal fun uniqueSlugs(labels: List<String>): List<String> {
    val taken = mutableSetOf<String>()

    return labels.mapIndexed { index, label ->
        val base = label.toSlug(fallback = "fixture-${index + 1}")
        val slug = generateSequence(1) { it + 1 }
            .map { if (it == 1) base else "$base-$it" }
            .first { taken.add(it) }

        slug
    }
}
