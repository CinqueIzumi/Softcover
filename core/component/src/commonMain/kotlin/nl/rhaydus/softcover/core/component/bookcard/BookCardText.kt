package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BookCardTextBlock(
    content: BookCardContent,
    titleStyle: TextStyle,
    titleMinLines: Int,
    titleMaxLines: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    progress: BookCardProgress? = null,
) {
    val typography = MaterialTheme.editorialTypography
    val eyebrow = content.eyebrow
    val byline = content.byline

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (eyebrow != null) {
            val eyebrowColor = when (eyebrow.tone) {
                BookCardEyebrow.Tone.Accent -> MaterialTheme.colorScheme.primary
                BookCardEyebrow.Tone.Error -> MaterialTheme.colorScheme.error
            }

            SoftcoverText(
                text = eyebrow.text,
                isLoading = isLoading,
                standIn = eyebrow.text,
                style = typography.eyebrowSmall,
                color = eyebrowColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        TitleRow(
            content = content,
            titleStyle = titleStyle,
            minLines = titleMinLines,
            maxLines = titleMaxLines,
            isLoading = isLoading,
        )

        if (byline != null) {
            SoftcoverText(
                text = byline,
                isLoading = isLoading,
                standIn = byline,
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        content.supportingLines.forEach { line ->
            SupportingLine(
                line = line,
                isLoading = isLoading,
            )
        }

        if (progress != null) {
            BookCardProgressBar(
                progress = progress,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun TitleRow(
    content: BookCardContent,
    titleStyle: TextStyle,
    minLines: Int,
    maxLines: Int,
    isLoading: Boolean,
) {
    val titleIcon = content.titleIcon

    val title = @Composable { modifier: Modifier ->
        SoftcoverText(
            text = content.title,
            isLoading = isLoading,
            standIn = content.title,
            style = titleStyle,
            color = MaterialTheme.colorScheme.onSurface,
            minLines = minLines,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    }

    if (titleIcon == null) {
        title(Modifier)

        return
    }

    val lineHeight = if (titleStyle.lineHeight.isSpecified) titleStyle.lineHeight else titleStyle.fontSize
    val lineHeightDp = with(LocalDensity.current) { lineHeight.toDp() }
    val iconSize = lineHeightDp * 0.85f

    val icon = drawableIconResource(
        icon = titleIcon,
        contentDescription = "",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = icon.getIconPainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = (lineHeightDp - iconSize) / 2)
                .size(iconSize)
                .skeletonMask(visible = isLoading),
        )

        title(Modifier.weight(1f))
    }
}

@Composable
private fun SupportingLine(
    line: BookCardLine,
    isLoading: Boolean,
) {
    val style = MaterialTheme.editorialTypography.bodySmall.copy(fontFeatureSettings = "tnum")
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    Row(verticalAlignment = Alignment.CenterVertically) {
        line.parts.forEachIndexed { index, part ->
            if (index > 0) {
                SoftcoverText(
                    text = " · ",
                    style = style,
                    color = color,
                    modifier = Modifier
                        .skeletonMask(visible = isLoading)
                        .clearAndSetSemantics {},
                )
            }

            when (part) {
                is BookCardLinePart.Text -> SoftcoverText(
                    text = part.text,
                    isLoading = isLoading,
                    standIn = part.text,
                    style = style,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                )

                is BookCardLinePart.Rating -> RatingLabel(
                    label = part.label,
                    contentDescription = part.contentDescription,
                    style = style,
                    color = color,
                    isLoading = isLoading,
                )
            }
        }
    }
}

@Composable
private fun RatingLabel(
    label: String,
    contentDescription: String,
    style: TextStyle,
    color: Color,
    isLoading: Boolean,
) {
    val icon = drawableIconResource(
        icon = SoftcoverIcon.StarFilled,
        contentDescription = "",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
    ) {
        Icon(
            painter = icon.getIconPainter(),
            contentDescription = null,
            tint = RatingGold,
            modifier = Modifier
                .size(12.dp)
                .skeletonMask(visible = isLoading),
        )

        SoftcoverText(
            text = label,
            isLoading = isLoading,
            standIn = label,
            style = style,
            color = color,
            maxLines = 1,
        )
    }
}
