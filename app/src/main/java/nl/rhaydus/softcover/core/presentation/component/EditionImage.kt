package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.modifier.shimmer

@Composable
fun EditionImage(
    edition: BookEdition?,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = edition?.url,
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(shape = RoundedCornerShape(4.dp)),
        contentDescription = "Book image",
        loading = { Box(modifier = Modifier.shimmer()) }
    )
}