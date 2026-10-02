package nl.rhaydus.softcover.core.component.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import nl.rhaydus.softcover.core.component.state.skeletonMask

/**
 * The app's text leaf. A null [text] means the value is unknown, so it masks like [isLoading]. While
 * masked it lays out [standIn] in the same [style], so [standIn] sizes the mask and should look like a
 * typical real value; the empty default masks nothing visible.
 */
@Composable
fun SoftcoverText(
    text: String?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    isLoading: Boolean = false,
    standIn: String = "",
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    autoSize: TextAutoSize? = null,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val isMasked = isLoading || text == null
    val shown = if (isMasked) standIn else text.orEmpty()
    val annotated = remember(shown) { AnnotatedString(shown) }

    SoftcoverTextLayout(
        text = annotated,
        isMasked = isMasked,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        minLines = minLines,
        overflow = overflow,
        softWrap = softWrap,
        autoSize = autoSize,
        inlineContent = emptyMap(),
        onTextLayout = onTextLayout,
    )
}

/** The [AnnotatedString] overload of [SoftcoverText]; while masked, [standIn] is laid out as plain text. */
@Composable
fun SoftcoverText(
    text: AnnotatedString?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    isLoading: Boolean = false,
    standIn: String = "",
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    autoSize: TextAutoSize? = null,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val isMasked = isLoading || text == null
    val standInText = remember(standIn) { AnnotatedString(standIn) }

    SoftcoverTextLayout(
        text = if (isMasked || text == null) standInText else text,
        isMasked = isMasked,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        minLines = minLines,
        overflow = overflow,
        softWrap = softWrap,
        autoSize = autoSize,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
    )
}

@Composable
private fun SoftcoverTextLayout(
    text: AnnotatedString,
    isMasked: Boolean,
    modifier: Modifier,
    style: TextStyle,
    color: Color,
    textAlign: TextAlign?,
    maxLines: Int,
    minLines: Int,
    overflow: TextOverflow,
    softWrap: Boolean,
    autoSize: TextAutoSize?,
    inlineContent: Map<String, InlineTextContent>,
    onTextLayout: ((TextLayoutResult) -> Unit)?,
) {
    Text(
        text = text,
        modifier = modifier.skeletonMask(visible = isMasked),
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        minLines = minLines,
        overflow = overflow,
        softWrap = softWrap,
        autoSize = autoSize,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout ?: {},
    )
}
