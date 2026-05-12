package nl.rhaydus.softcover.core.presentation.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import nl.rhaydus.softcover.core.presentation.component.SoftcoverImage
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography

private const val TABULAR_NUMS = "tnum"

@Composable
fun ShareCard(
    content: ShareContent,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = when (content) {
        is StatShareContent -> MaterialTheme.colorScheme.primary
        is BookShareContent,
        is QuoteShareContent,
        is YearRecapShareContent -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (content) {
        is StatShareContent -> MaterialTheme.colorScheme.onPrimary
        is BookShareContent,
        is QuoteShareContent,
        is YearRecapShareContent -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .size(
                width = ShareCardDimensions.Width,
                height = ShareCardDimensions.Height,
            )
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ShareCardDimensions.Padding),
        ) {
            when (content) {
                is BookShareContent -> BookShareCardBody(content)
                is StatShareContent -> StatShareCardBody(content)
                is QuoteShareContent -> QuoteShareCardBody(content)
                is YearRecapShareContent -> YearRecapShareCardBody(content)
            }

            Spacer(modifier = Modifier.weight(1f))

            ShareCardSignOff()
        }
    }
}

@Composable
private fun BookShareCardBody(content: BookShareContent) {
    Text(
        text = "FROM THE SHELF",
        style = MaterialTheme.editorialTypography.eyebrow,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(96.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            SoftcoverImage(
                model = content.coverUrl,
                contentDescription = "Cover of ${content.title}",
                isLoading = false,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = content.title,
                style = MaterialTheme.editorialTypography.display,
                color = LocalContentColor.current,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content.author.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            if (content.userRating != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Rated ${content.userRating}/10",
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (content.quote != null) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "“${content.quote}”",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatShareCardBody(content: StatShareContent) {
    Text(
        text = content.eyebrow.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrow,
        color = LocalContentColor.current.copy(alpha = 0.75f),
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = content.value.toString(),
        style = MaterialTheme.editorialTypography.statHero.copy(fontFeatureSettings = TABULAR_NUMS),
        color = LocalContentColor.current,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = content.caption,
        style = MaterialTheme.editorialTypography.body,
        color = LocalContentColor.current.copy(alpha = 0.75f),
    )
}

@Composable
private fun QuoteShareCardBody(content: QuoteShareContent) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            modifier = Modifier.align(Alignment.TopStart),
        )

        Column(modifier = Modifier.padding(top = 40.dp)) {
            Text(
                text = content.quote,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(20.dp))

            val byline = buildString {
                append(content.sourceAuthor.uppercase())
                append(" — ")
                append(content.sourceTitle.uppercase())

                if (content.page != null) {
                    append(" · p. ")
                    append(content.page)
                }
            }

            Text(
                text = byline,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun YearRecapShareCardBody(content: YearRecapShareContent) {
    Text(
        text = "${content.eyebrow.uppercase()} · ${content.year}",
        style = MaterialTheme.editorialTypography.eyebrow,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = content.headline,
        style = MaterialTheme.editorialTypography.display,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(24.dp))

    content.highlights.forEachIndexed { index, highlight ->
        if (index > 0) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "—",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = highlight,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
