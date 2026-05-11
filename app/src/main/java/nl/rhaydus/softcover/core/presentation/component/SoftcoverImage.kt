package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.util.SkeletonCrossfade

@Composable
fun SoftcoverImage(
    model: Any?,
    contentDescription: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    SkeletonCrossfade(
        isLoading = isLoading,
        modifier = modifier,
        label = "SoftcoverImage",
    ) { loading ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize().shimmer())
        } else {
            SubcomposeAsyncImage(
                model = model,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(modifier = Modifier.fillMaxSize().shimmer()) },
                contentScale = contentScale,
            )
        }
    }
}
