package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable

/**
 * One span of text inside a [RichTextParagraph], carrying the inline marks that apply to all of it.
 *
 * The three marks are independent flags rather than a sealed variant because they genuinely compose —
 * a run can be bold *and* italic *and* a spoiler at once — which is the case R2's sealed-variant rule
 * explicitly does not cover.
 *
 * [spoiler] hides this run behind a tap-to-reveal cover when rendered. It is a *per-run* mark, and is
 * unrelated to any whole-review spoiler gate a caller wraps around the text.
 */
@Immutable
data class RichTextRun(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val spoiler: Boolean = false,
)
