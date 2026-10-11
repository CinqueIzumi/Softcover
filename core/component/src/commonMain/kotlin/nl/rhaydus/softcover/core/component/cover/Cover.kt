package nl.rhaydus.softcover.core.component.cover

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalSharedTransitionScope

private const val COVER_ASPECT_RATIO = 2f / 3f

/**
 * The canonical book cover and the only way the app draws one — never reach for a raw image loader
 * for cover art; a surface that needs the raw request rather than this rendered component uses
 * [rememberCoverImageRequest] instead. Rendered as: a shimmer while [CoverUiModel.isLoading] or the
 * image load has nothing to show yet, [CoverlessTitleCover] (or a blank box, for purely decorative
 * surfaces) when [CoverUiModel.source] is `null` or fails to decode, or the loaded image otherwise.
 * When the source changes, the last loaded image stays up until the new one replaces it. Treatment —
 * elevation, corner radius, shadow tint, decode cap — comes from [CoverUiModel.variant]; no call site
 * hand-tunes a cover, and widths stay in the caller's [modifier] rather than in [CoverVariant].
 *
 * The reading hero backdrop and the full-screen viewer do not render through `Cover` — a `Crop` +
 * `blur` backdrop and a zoom/pan full-bleed viewer need their own composition, so those two surfaces
 * call [rememberCoverImageRequest] directly instead.
 *
 * **[model] is nullable, and that is the whole point.** Because mapping happens in a collector,
 * a cover model lands one state emission after the book it belongs to, so every caller —
 * a lazy grid resolving `covers[id]`, a sheet whose model is still null — has a frame where it has
 * an item but no cover. `Cover` answers that centrally by reserving the footprint [modifier] gives
 * it and drawing nothing, so the surrounding layout never moves. Callers pass the nullable value
 * straight through: they must not skip the item (the list then renders empty and pops in) and they
 * must not re-invent a local placeholder.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Cover(
    model: CoverUiModel?,
    modifier: Modifier = Modifier,
) {
    if (model == null) {
        Box(modifier = modifier.aspectRatio(COVER_ASPECT_RATIO))

        return
    }

    val request = rememberCoverImageRequest(model = model)

    val coverlessFallback = model.coverlessTitle?.takeIf { it.isNotBlank() }
    val contentDescription = coverlessFallback?.let { "Cover of $it" }

    val shape = model.variant.shape()
    val imageModifier = if (model.variant.elevation() > 0.dp) {
        Modifier
            .shadow(
                elevation = model.variant.elevation(),
                shape = shape,
                clip = false,
                ambientColor = model.variant.shadowColor(),
                spotColor = model.variant.shadowColor(),
            )
            .clip(shape)
    } else {
        Modifier.clip(shape)
    }

    val playMotion = playDecorativeMotion()
    val sharedScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    val containerModifier = if (
        playMotion &&
        model.sharedTransitionKey != null &&
        sharedScope != null &&
        animatedVisibilityScope != null
    ) {
        with(sharedScope) {
            modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = model.sharedTransitionKey),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        modifier
    }

    if (request == null) {
        Box(
            modifier = containerModifier
                .aspectRatio(COVER_ASPECT_RATIO)
                .clip(shape),
        ) {
            if (model.isLoading) {
                CoverShimmer()
            } else {
                CoverFallback(model = model)
            }
        }

        return
    }

    Box(
        modifier = containerModifier
            .aspectRatio(COVER_ASPECT_RATIO)
            .clip(shape),
    ) {
        CoverImage(
            request = request,
            imageModifier = imageModifier,
            contentDescription = contentDescription,
            model = model,
        )
    }
}

@Composable
private fun CoverImage(
    request: ImageRequest,
    imageModifier: Modifier,
    contentDescription: String?,
    model: CoverUiModel,
) {
    var settled by remember { mutableStateOf<Painter?>(null) }
    val previous = remember(request) { settled }
    val painter = rememberAsyncImagePainter(
        model = request,
        placeholder = previous,
        contentScale = ContentScale.Fit,
    )
    val state by painter.state.collectAsState()

    val context = LocalPlatformContext.current
    val inMemory = remember(request) {
        val cache = SingletonImageLoader.get(context).memoryCache
        val key = request.memoryCacheKey?.let { MemoryCache.Key(it) }

        key != null && cache?.get(key) != null
    }

    LaunchedEffect(state) {
        (state as? AsyncImagePainter.State.Success)?.let {
            settled = it.painter
        }
    }

    if (state is AsyncImagePainter.State.Error) {
        CoverFallback(model = model)

        return
    }

    val loaded = (state as? AsyncImagePainter.State.Success)?.painter ?: previous
    val intrinsic = loaded?.intrinsicSize
    val ratio = if (intrinsic != null && intrinsic.isSpecified && intrinsic.height > 0f) {
        intrinsic.width / intrinsic.height
    } else {
        COVER_ASPECT_RATIO
    }
    val loadingState = state as? AsyncImagePainter.State.Loading
    val hasNothingToShow = state is AsyncImagePainter.State.Empty ||
        (loadingState != null && loadingState.painter == null)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val width = maxWidth

        Box(
            modifier = imageModifier
                .width(width)
                .requiredHeight(width / ratio)
                .skeletonMask(
                    visible = hasNothingToShow && previous == null && inMemory.not(),
                    shape = RectangleShape,
                ),
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun CoverFallback(model: CoverUiModel) {
    if (model.coverlessTitle.isNullOrBlank().not()) {
        CoverlessTitleCover(model = model)
    } else {
        Box(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Persists a successfully-loaded remote cover's bytes against an edition, read back out of Coil's own
 * disk cache once the load completes. This is the persistence seam [rememberCoverImageRequest] calls
 * through — `:core:component` may not depend on Koin or on a domain use case, so the feature
 * wiring the persister supplies it via [LocalCoverImagePersister] instead.
 */
fun interface CoverImagePersister {
    suspend fun persist(
        editionId: Int,
        url: String,
        bytes: ByteArray,
    )
}

/**
 * The [CoverImagePersister] in scope, or `null` when nothing should persist — the Component Gallery
 * and every other preview leave this at its default `null` and simply skip persisting.
 */
val LocalCoverImagePersister = staticCompositionLocalOf<CoverImagePersister?> { null }

/**
 * Builds the [ImageRequest] for [model], or `null` when [CoverUiModel.source] is `null` — the caller
 * then renders the coverless rung. Public because two surfaces render a cover image without going
 * through [Cover] itself: the reading hero backdrop applies its own `Crop` + `blur(64.dp)` with no
 * shimmer and no coverless rung, and the full-screen viewer fills the screen with a zoom/pan
 * `graphicsLayer`.
 */
@Composable
fun rememberCoverImageRequest(model: CoverUiModel): ImageRequest? {
    val context = LocalPlatformContext.current
    val persister = LocalCoverImagePersister.current
    val coroutineScope = rememberCoroutineScope()
    val maxDecodePx = if (model.variant == CoverVariant.Shelf) 600 else null
    val playMotion = playDecorativeMotion()

    val source = model.source ?: return null

    return remember(
        source,
        maxDecodePx,
        playMotion,
    ) {
        val builder = when (source) {
            is CoverSource.Local -> ImageRequest.Builder(context).data(source.path)
            is CoverSource.Remote -> ImageRequest.Builder(context).data(source.url)
        }

        val cacheKeyUrl = when (source) {
            is CoverSource.Local -> source.cacheKey
            is CoverSource.Remote -> source.url
        }

        cacheKeyUrl?.let { key ->
            builder.memoryCacheKey(MemoryCache.Key(key))
            builder.placeholderMemoryCacheKey(MemoryCache.Key(key))
        }

        if (source is CoverSource.Remote && source.persistEditionId != null) {
            val editionId = source.persistEditionId
            val url = source.url
            val activePersister = persister

            if (activePersister != null) {
                builder.listener(
                    onSuccess = { _, _ ->
                        persistFromDiskCache(
                            coroutineScope = coroutineScope,
                            persister = activePersister,
                            imageLoader = SingletonImageLoader.get(context),
                            editionId = editionId,
                            url = url,
                        )
                    },
                )
            }
        }

        builder.crossfade(playMotion)

        if (maxDecodePx != null) {
            builder.size(maxDecodePx)
        }

        builder.build()
    }
}

/** The corner shape [Cover] clips this variant to; callers match a hover wash or ring to the visible cover. */
fun CoverVariant.shape(): RoundedCornerShape = when (this) {
    CoverVariant.Shelf -> Corners.md
    CoverVariant.Rounded -> Corners.lg
    CoverVariant.Tile,
    CoverVariant.Spotlight,
    -> Corners.sm

    CoverVariant.Raised,
    CoverVariant.Floating,
    CoverVariant.Jacket,
    CoverVariant.StackedJacket,
    CoverVariant.Flat,
    -> Corners.xs
}

private fun CoverVariant.elevation(): Dp = when (this) {
    CoverVariant.Flat,
    CoverVariant.Rounded,
    -> 0.dp

    CoverVariant.StackedJacket -> 3.dp
    CoverVariant.Tile,
    CoverVariant.Jacket,
    -> 4.dp

    CoverVariant.Shelf,
    CoverVariant.Raised,
    -> 6.dp

    CoverVariant.Floating -> 12.dp
    CoverVariant.Spotlight -> 20.dp
}

private fun CoverVariant.shadowColor(): Color = when (this) {
    CoverVariant.Floating -> Color.Black.copy(alpha = 0.6f)
    CoverVariant.Jacket,
    CoverVariant.Spotlight,
    -> Color.Black.copy(alpha = 0.5f)

    CoverVariant.StackedJacket -> Color.Black.copy(alpha = 0.35f)
    CoverVariant.Shelf,
    CoverVariant.Raised,
    CoverVariant.Tile,
    CoverVariant.Rounded,
    CoverVariant.Flat,
    -> Color.Unspecified
}

@OptIn(ExperimentalCoilApi::class)
private fun persistFromDiskCache(
    coroutineScope: CoroutineScope,
    persister: CoverImagePersister,
    imageLoader: ImageLoader,
    editionId: Int,
    url: String,
) {
    coroutineScope.launch {
        val diskCache = imageLoader.diskCache ?: return@launch
        val snapshot = diskCache.openSnapshot(url) ?: return@launch

        snapshot.use {
            persister.persist(
                editionId = editionId,
                url = url,
                bytes = diskCache.fileSystem.read(it.data) { readByteArray() },
            )
        }
    }
}

@Composable
private fun CoverShimmer() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .skeletonMask(
                visible = true,
                shape = RectangleShape,
            ),
    )
}
