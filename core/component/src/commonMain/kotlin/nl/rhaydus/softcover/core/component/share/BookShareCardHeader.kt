package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/** The eyebrow, cover, title and byline both book-flavoured cards open with. */
@Composable
internal fun BookShareCardHeader(
    eyebrow: String,
    coverUrl: String?,
    title: String,
    author: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SoftcoverText(
            text = eyebrow,
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(2f / 3f)
                .clip(Corners.sm),
        ) {
            RhaydusShimmerImage(
                model = coverUrl,
                contentDescription = "Cover of $title",
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        SoftcoverText(
            text = title,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (author.isNotBlank()) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            SoftcoverText(
                text = "BY ${author.uppercase()}",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
