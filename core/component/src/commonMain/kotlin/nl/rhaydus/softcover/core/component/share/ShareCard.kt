package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
fun ShareCard(
    content: ShareCardUiModel,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = when (content) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.primary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (content) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.onPrimary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.onSurface
    }

    val dimensions = ShareCardDimensions.forContent(content = content)

    val sizeModifier = if (dimensions.fixedHeight != null) {
        Modifier.requiredSize(
            width = dimensions.width,
            height = dimensions.fixedHeight,
        )
    } else {
        Modifier
            .requiredWidth(width = dimensions.width)
            .requiredHeightIn(min = dimensions.minHeight)
    }

    Surface(
        modifier = modifier
            .then(other = sizeModifier)
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.padding),
        ) {
            when (content) {
                is BookShareCardUiModel -> BookShareCardBody(content)
                is ReadingUpdateShareCardUiModel -> ReadingUpdateShareCardBody(content)
                is StatShareCardUiModel -> StatShareCardBody(content)
                is QuoteShareCardUiModel -> QuoteShareCardBody(content)
                is YearRecapShareCardUiModel -> YearRecapShareCardBody(content)
                is ReadingLifeShareCardUiModel -> ReadingLifeShareCardBody(content)
            }

            Spacer(modifier = Modifier.weight(1f))

            ShareCardSignOff()
        }
    }
}

@Composable
private fun ShareCardSignOff() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "SOFTCOVER",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.6f),
        )

        Text(
            text = "— · —",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.4f),
        )
    }
}
