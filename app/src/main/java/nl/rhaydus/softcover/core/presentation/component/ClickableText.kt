package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle

@Composable
fun ClickableText(
    annotatedText: AnnotatedString,
    style: TextStyle,
    handleUrlClick: (String) -> Unit,
) {
    var textLayoutResult by remember {
        mutableStateOf<TextLayoutResult?>(null)
    }

    Text(
        text = annotatedText,
        style = style,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val layoutResult = textLayoutResult ?: return@detectTapGestures

                    val position = layoutResult.getOffsetForPosition(tapOffset)

                    annotatedText
                        .getStringAnnotations(start = position, end = position)
                        .firstOrNull()
                        ?.let { annotation -> handleUrlClick(annotation.item) }
                }
            },
        onTextLayout = { layout ->
            textLayoutResult = layout
        }
    )
}