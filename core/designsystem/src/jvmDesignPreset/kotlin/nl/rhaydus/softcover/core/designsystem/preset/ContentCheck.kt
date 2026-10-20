package nl.rhaydus.softcover.core.designsystem.preset

import java.io.File

private val backticked = Regex("`([A-Za-z0-9][A-Za-z0-9_.-]*)`")
private val cssVariable = Regex("var\\(--([A-Za-z0-9_.-]+)")
private val hexColor = Regex("#(?:[0-9a-fA-F]{8}|[0-9a-fA-F]{6}|[0-9a-fA-F]{4}|[0-9a-fA-F]{3})\\b")

/**
 * Checks the hand-written preset content against the generated tokens. Absent files pass, so the gate can
 * sit in `check` before the content exists.
 *
 * A backticked word in the brand book counts as a token reference when it is hyphenated and starts like a
 * token (`space-`, `on-`, `surface-`), so a mistyped token fails while code words in backticks do not.
 */
internal object ContentCheck {
    private const val BRAND_BOOK = "README.md"
    private const val BUNDLE_CSS = "components/bundle.css"

    /** Problems found in the brand book and the bundle stylesheet under [designDir]; empty when clean. */
    fun problems(
        designDir: File,
        tokenNames: Set<String>,
    ): List<String> {
        val brandBook = designDir.resolve(BRAND_BOOK).takeIf { it.isFile }
        val css = designDir.resolve(BUNDLE_CSS).takeIf { it.isFile }
        val prefixes = tokenNames.map { it.substringBefore('-') }.toSet()

        val unknownInBrandBook = brandBook?.let { file ->
            references(file, backticked) { '-' in it && it.substringBefore('-') in prefixes }
        }.orEmpty()
        val unknownInCss = css?.let { references(it, cssVariable) { true } }.orEmpty()
        val hardCoded = css?.let { hardCodedColours(it) }.orEmpty()

        return (unknownInBrandBook + unknownInCss)
            .filter { (name, _) -> name !in tokenNames }
            .map { (name, location) -> "$location: unknown token $name" } + hardCoded
    }

    /** Token-like words in [file] as (name, `file:line`) pairs. */
    private fun references(
        file: File,
        pattern: Regex,
        isTokenLike: (String) -> Boolean,
    ): List<Pair<String, String>> = file.readLines().flatMapIndexed { index, line ->
        pattern.findAll(line)
            .map { it.groupValues[1] }
            .filter(isTokenLike)
            .map { it to "${file.name}:${index + 1}" }
            .toList()
    }

    private fun hardCodedColours(file: File): List<String> = file.readLines().flatMapIndexed { index, line ->
        hexColor.findAll(line).map { "${file.name}:${index + 1}: raw colour ${it.value}; use a token" }.toList()
    }
}
