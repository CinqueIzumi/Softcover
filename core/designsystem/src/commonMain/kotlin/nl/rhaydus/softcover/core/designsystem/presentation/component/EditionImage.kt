package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.book.domain.usecase.PersistEditionImageUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalSharedTransitionScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import org.koin.compose.koinInject

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EditionImage(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    fallbackCoverUrl: String? = null,
    coverlessTitle: String? = null,
    elevation: Dp = 0.dp,
    cornerRadius: Dp = 4.dp,
    shadowColor: Color = Color.Unspecified,
    sharedTransitionKey: String? = null,
    maxDecodePx: Int? = null,
) {
    val request = rememberEditionImageRequest(
        edition = edition,
        defaultEdition = defaultEdition,
        fallbackCoverUrl = fallbackCoverUrl,
        maxDecodePx = maxDecodePx,
    )

    val coverlessFallback = coverlessTitle?.takeIf { it.isNotBlank() }

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

    val sharedScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    val containerModifier = if (
        sharedTransitionKey != null &&
        sharedScope != null &&
        animatedVisibilityScope != null
    ) {
        with(sharedScope) {
            modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = sharedTransitionKey),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        modifier
    }

    // The loading path uses a bespoke SubcomposeAsyncImage rather than RhaydusShimmerImage because
    // it needs both a custom success slot (BoxWithConstraints + aspect-ratio from the intrinsic
    // painter size) and a custom error slot (CoverlessTitleCover fallback). RhaydusPlaceholderImage
    // only exposes the placeholder slot; routing through it would require dropping these, changing
    // behavior. The shimmer + SkeletonCrossfade primitives (from designsystem-core) are reused directly.
    SkeletonCrossfade(
        isLoading = isLoading && request == null,
        modifier = containerModifier
            .aspectRatio(2f / 3f)
            .clip(shape),
        label = "EditionImage",
    ) { loading ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize().shimmer())
        } else if (request == null) {
            if (coverlessFallback != null) {
                CoverlessTitleCover(title = coverlessFallback)
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        } else {
            SubcomposeAsyncImage(
                model = request,
                contentDescription = "Book edition image",
                modifier = Modifier.fillMaxSize(),
                loading = { Box(modifier = Modifier.fillMaxSize().shimmer()) },
                error = {
                    if (coverlessFallback != null) {
                        CoverlessTitleCover(title = coverlessFallback)
                    } else {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                },
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
    }
}

/**
 * The final rung of [EditionImage]'s fallback chain: when no cover source resolves (and the data is
 * not still loading), a book with a known title is rendered as a coverless cover rather than a blank
 * tile — a filled [surfaceContainerHighest] card carrying the title in the editorial Fraunces voice,
 * autosized to fit the cover. Used by the cover-only library layouts, where there is no adjacent
 * title text to fall back on. Imagery cards keep a container shade (DS §2.1), so this never reaches
 * for primary.
 */
@Composable
private fun CoverlessTitleCover(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 12.sp,
                maxFontSize = MaterialTheme.editorialTypography.headlineMedium.fontSize,
                stepSize = 1.sp,
            ),
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun rememberEditionImageRequest(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    fallbackCoverUrl: String? = null,
    maxDecodePx: Int? = null,
): ImageRequest? {
    val context = LocalPlatformContext.current
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

    return remember(
        resolution,
        maxDecodePx,
    ) {
        val builder = ImageRequest.Builder(context).data(resolution.source)

        resolution.cacheKeyUrl?.let { key ->
            builder.memoryCacheKey(MemoryCache.Key(key))

            if (resolution.isLocal) {
                builder.placeholderMemoryCacheKey(MemoryCache.Key(key))
            }
        }

        resolution.persistEditionId?.let { editionId ->
            val sourceUrl = resolution.source
            val useCase = persistEditionImageUseCase ?: return@let
            builder.listener(
                onSuccess = { _, _ ->
                    persistFromDiskCache(
                        coroutineScope = coroutineScope,
                        persistEditionImageUseCase = useCase,
                        imageLoader = SingletonImageLoader.get(context),
                        editionId = editionId,
                        url = sourceUrl,
                    )
                },
            )
        }

        if (maxDecodePx != null) {
            builder.size(maxDecodePx)
        }

        builder.build()
    }
}

/**
 * Resolves the Coil image source — a local-file path or a remote URL, both passed to Coil as a
 * [String] model — for a book cover, preferring the user's selected [edition], then the
 * [defaultEdition], then [fallbackCoverUrl]. Shared so non-Composable surfaces (e.g. the
 * reading-session notification) render the exact same edition cover the in-app [EditionImage]
 * shows. Returns null when no source is known.
 */
fun resolveEditionImageSource(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    fallbackCoverUrl: String?,
): String? = resolveEditionImage(
    edition = edition,
    defaultEdition = defaultEdition,
    fallbackCoverUrl = fallbackCoverUrl,
)?.source

private data class EditionImageResolution(
    val source: String,
    val isLocal: Boolean,
    val cacheKeyUrl: String?,
    val persistEditionId: Int?,
)

private fun resolveEditionImage(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    fallbackCoverUrl: String?,
): EditionImageResolution? {
    val localEdition = edition?.takeIf { it.existingLocalSource() != null }
        ?: defaultEdition?.takeIf { it.existingLocalSource() != null }

    if (localEdition != null) {
        return EditionImageResolution(
            source = localEdition.existingLocalSource()!!,
            isLocal = true,
            cacheKeyUrl = localEdition.url,
            persistEditionId = null,
        )
    }

    val urlEdition = edition?.takeIf { it.url != null }
        ?: defaultEdition?.takeIf { it.url != null }

    if (urlEdition != null) {
        return EditionImageResolution(
            source = urlEdition.url!!,
            isLocal = false,
            cacheKeyUrl = urlEdition.url,
            persistEditionId = urlEdition.id,
        )
    }

    val fallback = fallbackCoverUrl ?: return null

    return EditionImageResolution(
        source = fallback,
        isLocal = false,
        cacheKeyUrl = fallback,
        persistEditionId = null,
    )
}

private fun BookEdition.existingLocalSource(): String? =
    localImageSourceOrNull(localImagePath)

/**
 * Returns [localImagePath] when it points at a persisted local cover file that exists on disk
 * (a model Coil can load directly), or null otherwise. Android checks the filesystem; iOS has no
 * persisted-cover support yet.
 */
expect fun localImageSourceOrNull(localImagePath: String?): String?

@OptIn(ExperimentalCoilApi::class)
private fun persistFromDiskCache(
    coroutineScope: CoroutineScope,
    persistEditionImageUseCase: PersistEditionImageUseCase,
    imageLoader: ImageLoader,
    editionId: Int,
    url: String,
) {
    coroutineScope.launch {
        val diskCache = imageLoader.diskCache ?: return@launch
        val snapshot = diskCache.openSnapshot(url) ?: return@launch

        snapshot.use {
            persistEditionImageUseCase(
                editionId = editionId,
                url = url,
                bytes = diskCache.fileSystem.read(it.data) { readByteArray() },
            )
        }
    }
}
