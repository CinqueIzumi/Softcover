package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.domain.model.BookEdition

@Composable
fun EditionImage(
    edition: BookEdition?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    SoftcoverImage(
        url = edition?.url,
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(shape = RoundedCornerShape(4.dp)),
        contentDescription = "Book edition image",
        isLoading = isLoading,
    )
}