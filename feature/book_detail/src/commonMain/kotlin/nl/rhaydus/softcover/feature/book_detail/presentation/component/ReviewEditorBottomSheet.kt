package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight
import nl.rhaydus.softcover.core.domain.model.ReviewDocument

/**
 * Editorial editing surface for the user's personal review. The body is a rich-text field: selecting
 * text and tapping the [ReviewFormattingToolbar] marks it bold, italic, or as a spoiler, and the field
 * styles the marks live (spoiler runs get a translucent wash so the author still reads their words).
 * "Save" publishes the structured review to Hardcover (offline-replayable); it is owned by Hardcover,
 * so it round-trips and is overwritten on the next refresh. "Delete" clears it and is only offered once
 * a review exists. The "Contains spoilers" switch is the whole-review gate, separate from inline marks.
 */
@Composable
internal fun ReviewEditorBottomSheet(
    initialDocument: ReviewDocument,
    initialHasSpoilers: Boolean,
    canDelete: Boolean,
    onSave: (ReviewDocument, Boolean) -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    val initialBuffer = remember { documentToEditorBuffer(document = initialDocument) }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialBuffer.text,
                selection = TextRange(initialBuffer.text.length),
            ),
        )
    }

    var marks by remember { mutableStateOf(initialBuffer.marks) }

    var hasSpoilers by remember { mutableStateOf(initialHasSpoilers) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val spoilerHighlight: Color = MaterialTheme.colorScheme.spoilerEditorHighlight

    val onSurfaceColor: Color = MaterialTheme.colorScheme.onSurface

    val visualTransformation = remember(marks, spoilerHighlight) {
        VisualTransformation { input ->
            TransformedText(
                text = buildEditorAnnotatedString(
                    text = input.text,
                    marks = marks,
                    spoilerHighlight = spoilerHighlight,
                ),
                offsetMapping = OffsetMapping.Identity,
            )
        }
    }

    val selection = textFieldValue.selection
    val hasSelection: Boolean = selection.collapsed.not()
    val selectionStart: Int = selection.min
    val selectionEnd: Int = selection.max

    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Your review",
                headline = "Write a few words.",
                description = "Share what stayed with you — your take helps other readers find their next book.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            ReviewFormattingToolbar(
                isBold = isRangeMarked(
                    marks = marks,
                    start = selectionStart,
                    end = selectionEnd,
                    type = ReviewMarkType.BOLD,
                ),
                isItalic = isRangeMarked(
                    marks = marks,
                    start = selectionStart,
                    end = selectionEnd,
                    type = ReviewMarkType.ITALIC,
                ),
                isSpoiler = isRangeMarked(
                    marks = marks,
                    start = selectionStart,
                    end = selectionEnd,
                    type = ReviewMarkType.SPOILER,
                ),
                enabled = hasSelection,
                onToggle = { type ->
                    marks = toggleMark(
                        marks = marks,
                        start = selectionStart,
                        end = selectionEnd,
                        type = type,
                    )
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = textFieldValue,
                onValueChange = { updated ->
                    if (updated.text != textFieldValue.text) {
                        marks = applyEditToMarks(
                            marks = marks,
                            oldText = textFieldValue.text,
                            newText = updated.text,
                        )
                    }

                    textFieldValue = updated
                },
                textStyle = MaterialTheme.editorialTypography.body.copy(
                    color = onSurfaceColor,
                    fontStyle = FontStyle.Normal,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "What stayed with you?",
                                style = MaterialTheme.editorialTypography.body.copy(
                                    fontStyle = FontStyle.Normal,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contains spoilers",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Readers will need to tap to reveal the whole review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = hasSpoilers,
                    onCheckedChange = { hasSpoilers = it },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            RhaydusButton(
                label = "Save",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                enabled = textFieldValue.text.isBlank().not(),
                onClick = {
                    onSave(
                        editorBufferToDocument(
                            text = textFieldValue.text,
                            marks = marks,
                        ),
                        hasSpoilers,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (canDelete) {
                Spacer(modifier = Modifier.height(8.dp))

                RhaydusButton(
                    label = "Delete review",
                    style = ButtonStyle.TEXT,
                    size = ButtonSize.M,
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun buildEditorAnnotatedString(
    text: String,
    marks: List<ReviewMark>,
    spoilerHighlight: Color,
): AnnotatedString = buildAnnotatedString {
    append(text)

    marks.forEach { mark ->
        val start = mark.start.coerceIn(
            0,
            text.length,
        )
        val end = mark.end.coerceIn(
            start,
            text.length,
        )

        if (start >= end) return@forEach

        val style = when (mark.type) {
            ReviewMarkType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)

            ReviewMarkType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)

            ReviewMarkType.SPOILER -> SpanStyle(background = spoilerHighlight)
        }

        addStyle(
            style = style,
            start = start,
            end = end,
        )
    }
}
