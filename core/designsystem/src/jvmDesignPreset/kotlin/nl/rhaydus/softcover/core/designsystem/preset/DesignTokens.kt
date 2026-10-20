package nl.rhaydus.softcover.core.designsystem.preset

import java.io.File

private const val SCHEMA_VERSION = 1
private const val REPOSITORY = "CinqueIzumi/Softcover"
private const val PACKAGE_PATH = "core/designsystem"
private val validName = Regex("^[A-Za-z0-9][A-Za-z0-9_.-]{0,63}$")

/** The whole `tokens.json` document, assembled from the module's own token objects. */
internal object DesignTokens {
    fun build(fontsDir: File): Map<String, Any?> {
        val document = linkedMapOf(
            "name" to "Softcover",
            "version" to SCHEMA_VERSION,
            "meta" to linkedMapOf("source" to "github", "repo" to REPOSITORY, "package" to PACKAGE_PATH),
            "color" to linkedMapOf(
                "themes" to PresetTheme.entries.map { linkedMapOf("id" to it.id, "name" to it.displayName) },
                "tokens" to ColorTokens.tokens(),
            ),
            "type" to TypeTokens.type(fontsDir),
            "spacing" to SpacingTokens.spacing(),
            "radius" to SpacingTokens.radius(),
        )
        checkNames(names(document))

        return document
    }

    /** Every token name across colour, spacing and radius, in file order. */
    fun names(document: Map<String, Any?>): List<String> = listOf("color", "spacing", "radius").flatMap { section ->
        val tokens = (document.getValue(section) as Map<*, *>)["tokens"] as List<*>

        tokens.map { (it as Map<*, *>)["name"] as String }
    }

    private fun checkNames(names: List<String>) {
        val invalid = names.filterNot { validName.matches(it) }
        check(invalid.isEmpty()) { "Invalid token names: $invalid" }

        val duplicates = names.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        check(duplicates.isEmpty()) { "Duplicate token names: $duplicates" }
    }
}
