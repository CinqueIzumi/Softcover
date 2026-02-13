package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import nl.rhaydus.softcover.core.presentation.modifier.shimmer

@Composable
fun SoftcoverImage(
    url: String?,
    contentDescription: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier.shimmer(isLoading = isLoading),
        loading = { Box(modifier = Modifier.shimmer()) },
        contentScale = contentScale,
    )
}