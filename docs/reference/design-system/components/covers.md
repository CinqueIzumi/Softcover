# Components — Covers

### Rhaydus shimmer image

The canonical loader for non-cover images (avatars, author photos, any free-aspect imagery). Provided by the foundation **`designsystem-image`** module (`nl.rhaydus.designsystem.image.RhaydusShimmerImage`). Use it rather than reaching for a raw image library; it shares the project's Coil loader, shimmer placeholder (suppressed under reduced motion), and `SkeletonCrossfade` behavior. Signature: `(model: Any?, contentDescription: String?, modifier, contentScale, isLoading)`. `Cover` is for book covers only — everything else uses `RhaydusShimmerImage`.
