package nl.rhaydus.softcover.core.domain.util

/**
 * Normalizes a raw scanned/typed ISBN into the canonical digit form Hardcover stores
 * (`isbn_10` / `isbn_13`), or returns `null` when the input is not a plausible book ISBN.
 *
 * Strips hyphens and whitespace, upper-cases the ISBN-10 `X` check digit, and keeps only values
 * that are a valid ISBN-10 (10 chars, digits with an optional trailing `X`) or ISBN-13 (13 digits).
 * An EAN-8 barcode — which the scanner can technically read but which is never a book ISBN — is
 * rejected here so it never reaches the lookup query.
 */
object IsbnNormalizer {

    private val ISBN_10_REGEX = Regex("^\\d{9}[\\dX]$")
    private val ISBN_13_REGEX = Regex("^\\d{13}$")

    fun normalize(raw: String): String? {
        val cleaned = raw
            .filterNot { it == '-' || it.isWhitespace() }
            .uppercase()

        return cleaned.takeIf { ISBN_10_REGEX.matches(it) || ISBN_13_REGEX.matches(it) }
    }
}
