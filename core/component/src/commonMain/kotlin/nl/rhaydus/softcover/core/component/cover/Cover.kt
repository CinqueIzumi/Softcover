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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalSharedTransitionScope

// Book covers are 2:3 everywhere, in both the loaded and the not-yet-mapped branch.
private const val COVER_ASPECT_RATIO = 2f / 3f

/**
 * A book/edition cover, rendered as: a shimmer while [CoverUiModel.isLoading] and nothing has
 * resolved yet, [CoverlessTitleCover] (or a blank box, for purely decorative surfaces) when
 * [CoverUiModel.source] is `null` or fails to decode, or the loaded image otherwise. Treatment —
 * elevation, corner radius, shadow tint, decode cap — comes from [CoverDimensions.forVariant], keyed
 * off [CoverUiModel.variant]; `Cover` itself never branches on the variant directly.
 *
 * [ReadingHeroBackdrop][CoverVariant.ReadingHeroBackdrop] and
 * [FullScreenViewer][CoverVariant.FullScreenViewer] do not render through `Cover` — a `Crop` + `blur`
 * backdrop and a zoom/pan full-bleed viewer need their own composition, so those two surfaces call
 * [rememberCoverImageRequest] directly instead.
 *
 * **[model] is nullable, and that is the whole point.** Because mapping happens in a collector
 * (§7.2 R9), a cover model lands one state emission after the book it belongs to, so every caller —
 * a lazy grid resolving `covers[id]`, a sheet whose model is still null — has a frame where it has
 * an item but no cover. `Cover` answers that centrally by reserving the footprint [modifier] gives
 * it and drawing nothing, so the surrounding layout never moves. Callers pass the nullable value
 * straight through: they must not skip the item (the list then renders empty and pops in) and they
 * must not re-invent a local placeholder, which is how four features grew four copies of the same
 * `CoverOrPlaceholder` before this was centralised.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Cover(
    model: CoverUiModel?,
    modifier: Modifier = Modifier,
) {
    if (model == null) {
        // The same 2:3 the loaded branch applies below — without it the placeholder would take the
        // caller's width and no height, collapsing the very layout this branch exists to hold open.
        Box(modifier = modifier.aspectRatio(COVER_ASPECT_RATIO))

        return
    }

    val request = rememberCoverImageRequest(model = model)
    val dimensions = CoverDimensions.forVariant(variant = model.variant)

    val coverlessFallback = model.coverlessTitle?.takeIf { it.isNotBlank() }

    val shape = RoundedCornerShape(dimensions.cornerRadius)
    val imageModifier = if (dimensions.elevation > 0.dp) {
        Modifier
            .shadow(
                elevation = dimensions.elevation,
                shape = shape,
                clip = false,
                ambientColor = dimensions.shadowColor,
                spotColor = dimensions.shadowColor,
            )
            .clip(shape)
    } else {
        Modifier.clip(shape)
    }

    val sharedScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    val containerModifier = if (
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

    // The loading path uses a bespoke SubcomposeAsyncImage rather than RhaydusShimmerImage because
    // it needs both a custom success slot (BoxWithConstraints + aspect-ratio from the intrinsic
    // painter size) and a custom error slot (CoverlessTitleCover fallback). RhaydusPlaceholderImage
    // only exposes the placeholder slot; routing through it would require dropping these, changing
    // behavior. The shimmer + SkeletonCrossfade primitives (from designsystem-core) are reused directly.
    SkeletonCrossfade(
        isLoading = model.isLoading && request == null,
        modifier = containerModifier
            .aspectRatio(COVER_ASPECT_RATIO)
            .clip(shape),
        label = "Cover",
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
                contentDescription = "Book cover image",
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
                            contentDescription = "Book cover image",
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
 * Persists a successfully-loaded remote cover's bytes against an edition, read back out of Coil's own
 * disk cache once the load completes. This is the persistence seam [rememberCoverImageRequest] calls
 * through — `:core:component` may not depend on Koin or on a domain use case (§ 7.4), so the feature
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
 * and every other preview leave this at its default `null` and simply skip persisting, replacing the
 * old `LocalInspectionMode` guard `EditionImage` used for the same purpose.
 */
val LocalCoverImagePersister = staticCompositionLocalOf<CoverImagePersister?> { null }

/**
 * Builds the [ImageRequest] for [model], or `null` when [CoverUiModel.source] is `null` — the caller
 * then renders the coverless rung. Public because two surfaces render a cover image without going
 * through [Cover] itself: [ReadingHeroBackdrop][CoverVariant.ReadingHeroBackdrop] applies its own
 * `Crop` + `blur(64.dp)` with no shimmer and no coverless rung, and
 * [FullScreenViewer][CoverVariant.FullScreenViewer] fills the screen with a zoom/pan `graphicsLayer`.
 */
@Composable
fun rememberCoverImageRequest(model: CoverUiModel): ImageRequest? {
    val context = LocalPlatformContext.current
    val persister = LocalCoverImagePersister.current
    val coroutineScope = rememberCoroutineScope()
    val maxDecodePx = CoverDimensions.forVariant(variant = model.variant).maxDecodePx

    val source = model.source ?: return null

    return remember(
        source,
        maxDecodePx,
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

            if (source is CoverSource.Local) {
                builder.placeholderMemoryCacheKey(MemoryCache.Key(key))
            }
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

        if (maxDecodePx != null) {
            builder.size(maxDecodePx)
        }

        builder.build()
    }
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

/**
 * The previews render [CoverUiModel.previews] rather than re-declaring their own values, so the
 * preview set and the Component Gallery's fixture set are one list and cannot drift (R5).
 */
@StandardPreview
@Composable
private fun CoverWithArtPreview() {
    SoftcoverTheme {
        Cover(
            model = CoverUiModel.previews.first { it.source != null },
            modifier = Modifier.width(140.dp),
        )
    }
}

@StandardPreview
@Composable
private fun CoverCoverlessLongTitlePreview() {
    SoftcoverTheme {
        val longTitleFixture = CoverUiModel.previews.first {
            it.source == null && it.coverlessTitle.orEmpty().length > COVERLESS_LONG_TITLE_FLOOR
        }

        Cover(
            model = longTitleFixture,
            modifier = Modifier.width(140.dp),
        )
    }
}

private const val COVERLESS_LONG_TITLE_FLOOR = 30

/** A thumbnail-sized tile: the coverless jacket degrades to a single initial at this width. */
@StandardPreview
@Composable
private fun CoverCoverlessThumbnailPreview() {
    SoftcoverTheme {
        Cover(
            model = CoverUiModel.previews.first { it.variant == CoverVariant.EditionListRow },
            modifier = Modifier.width(48.dp),
        )
    }
}

@StandardPreview
@Composable
private fun CoverLoadingPreview() {
    SoftcoverTheme {
        Cover(
            model = CoverUiModel.previews.first { it.isLoading },
            modifier = Modifier.width(140.dp),
        )
    }
}
