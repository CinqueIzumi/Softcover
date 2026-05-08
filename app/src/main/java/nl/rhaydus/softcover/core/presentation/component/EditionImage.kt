package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.feature.books.domain.usecase.PersistEditionImageUseCase
import org.koin.compose.koinInject

@Composable
fun EditionImage(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    fallbackCoverUrl: String? = null,
    elevation: Dp = 0.dp,
    cornerRadius: Dp = 4.dp,
    shadowColor: Color = Color.Unspecified,
) {
    val request = rememberEditionImageRequest(
        edition = edition,
        defaultEdition = defaultEdition,
        fallbackCoverUrl = fallbackCoverUrl,
    )

    val shape = RoundedCornerShape(cornerRadius)
    val imageModifier = if (elevation > 0.dp) {
        Modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(shape)
    } else {
        Modifier.clip(shape)
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = "Book edition image",
        modifier = modifier
            .aspectRatio(2f / 3f)
            .shimmer(isLoading = isLoading),
        loading = { Box(modifier = Modifier.fillMaxSize().shimmer()) },
        success = { state ->
            val intrinsic = state.painter.intrinsicSize
            val ratio = if (intrinsic.isSpecified && intrinsic.height > 0f) {
                intrinsic.width / intrinsic.height
            } else {
                2f / 3f
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val width = maxWidth
                Image(
                    painter = state.painter,
                    contentDescription = "Book edition image",
                    modifier = imageModifier
                        .width(width)
                        .requiredHeight(width / ratio),
                    contentScale = ContentScale.Fit,
                )
            }
        },
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun rememberEditionImageRequest(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    fallbackCoverUrl: String? = null,
): ImageRequest? {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val persistEditionImageUseCase = if (isInspection) {
        null
    } else {
        koinInject<PersistEditionImageUseCase>()
    }
    val coroutineScope = rememberCoroutineScope()

    val resolution = resolveEditionImage(
        edition = edition,
        defaultEdition = defaultEdition,
        fallbackCoverUrl = fallbackCoverUrl,
    ) ?: return null

    return remember(resolution) {
        val builder = ImageRequest.Builder(context).data(resolution.source)

        resolution.cacheKeyUrl?.let { key ->
            builder.memoryCacheKey(MemoryCache.Key(key))

            if (resolution.source is File) {
                builder.placeholderMemoryCacheKey(MemoryCache.Key(key))
            }
        }

        resolution.persistEditionId?.let { editionId ->
            val sourceUrl = resolution.source as? String ?: return@let
            val useCase = persistEditionImageUseCase ?: return@let
            builder.listener(
                onSuccess = { _, _ ->
                    persistFromDiskCache(
                        coroutineScope = coroutineScope,
                        persistEditionImageUseCase = useCase,
                        imageLoader = context.imageLoader,
                        editionId = editionId,
                        url = sourceUrl,
                    )
                },
            )
        }

        builder.build()
    }
}

private data class EditionImageResolution(
    val source: Any,
    val cacheKeyUrl: String?,
    val persistEditionId: Int?,
)

private fun resolveEditionImage(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    fallbackCoverUrl: String?,
): EditionImageResolution? {
    val localEdition = edition?.takeIf { it.existingLocalFile() != null }
        ?: defaultEdition?.takeIf { it.existingLocalFile() != null }

    if (localEdition != null) {
        return EditionImageResolution(
            source = localEdition.existingLocalFile()!!,
            cacheKeyUrl = localEdition.url,
            persistEditionId = null,
        )
    }

    val urlEdition = edition?.takeIf { it.url != null }
        ?: defaultEdition?.takeIf { it.url != null }

    if (urlEdition != null) {
        return EditionImageResolution(
            source = urlEdition.url!!,
            cacheKeyUrl = urlEdition.url,
            persistEditionId = urlEdition.id,
        )
    }

    val fallback = fallbackCoverUrl ?: return null

    return EditionImageResolution(
        source = fallback,
        cacheKeyUrl = fallback,
        persistEditionId = null,
    )
}

private fun BookEdition.existingLocalFile(): File? =
    localImagePath?.let(::File)?.takeIf { it.exists() }

@OptIn(ExperimentalCoilApi::class)
private fun persistFromDiskCache(
    coroutineScope: CoroutineScope,
    persistEditionImageUseCase: PersistEditionImageUseCase,
    imageLoader: coil.ImageLoader,
    editionId: Int,
    url: String,
) {
    coroutineScope.launch {
        val diskCache = imageLoader.diskCache ?: return@launch
        val snapshot = diskCache.openSnapshot(url) ?: return@launch

        snapshot.use {
            persistEditionImageUseCase(
                editionId = editionId,
                source = it.data.toFile(),
            )
        }
    }
}
