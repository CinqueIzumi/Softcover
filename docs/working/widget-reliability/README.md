# Widget reliability

Home-screen widgets fail with Glance's "Can't show content" and feel unresponsive. Three fixes: deliver every
widget image by `Uri` instead of as a bitmap inside the update, give every widget a real loading state with
text-first rendering, and restrict Random pick to lists the app caches locally.

## Now

- **State:** written 2026-10-17; nothing built. Cause reproduced on the minified build (Medium_Phone): rapid
  Trending paging → `IllegalArgumentException: RemoteViews for widget update exceeds maximum bitmap memory
  usage (used: 17667232, max: 15552000)`.
- **Next:** step A1, stop for approval of the store / provider shape.
- **Verification:** none yet.
- **Uncommitted:** this README.

## Why

- `SizeMode.Responsive` composes the layout once per declared size, and each size's `RemoteViews` carries its
  own copy of every `ImageProvider(Bitmap)`. Trending declares 8 sizes × 2 covers decoded for the largest
  cover (222dp, ~1–1.4 MB each); Currently reading declares 15 sizes with cover, backdrop and wave bitmaps.
- Android caps one update at 1.5 × screen pixels × 4 bytes. Over the cap, `AppWidgetManager.updateAppWidget`
  throws, Glance swaps in its error layout, and the next tap starts a new session that may fit again: the
  flicker in the user's recording (Xiaomi 15T).
- `Content.Loading` renders nothing, so every cold session shows an empty box first.
- Random pick offers every user list (`sourceChipsFor` reads `getAllUserLists()`), but only lists in
  `GetEnabledListIdsAsFlowUseCase` have their books in Room, so a disabled list gives an empty pool.

## Decisions

- Images reach the launcher as `content://` URIs from one read-only provider; no widget update carries a bitmap.
- Image files are content-addressed, rendered once at their displayed size, touched per render, pruned after 7 days untouched.
- The provider is exported, read-only, and serves only file names from its own directory.
- Loading: a cold session shows frame + indicator; after a tap, text renders first with cover placeholders.
- Trending prefetches the neighbouring pages' cover files.
- Covers render at the largest slot their widget displays (no fixed 86dp), since files carry no bitmap budget.
- Random pick hides lists not enabled for caching, Owned included.
- A placed Random pick whose list was disabled shows an explanation and opens its settings on tap.
- The quote widget has no bitmaps; long quotes shrink to fit the real widget size instead of truncating at fixed tiers.
- The `FullScreenCoverScreen` `NotSerializableException` crash is a separate fix.

## Steps

| Step | Scope | Status |
|---|---|---|
| A1 | `WidgetImageStore` (bitmap → file → `Uri`, touch, prune from the maintenance worker) and `WidgetImageProvider` + manifest entry + keep contract; design first, stop for approval | [ ] |
| A2 | Trending: covers by `Uri`; cover rendered at its displayed size | [ ] |
| A3 | Random pick: cover, backdrop and gradient masks by `Uri` (masks keep their tint) | [ ] |
| A4 | Currently reading: cover, backdrop, wave track / indicator and masks by `Uri` | [ ] |
| A5 | detekt `ForbiddenMethodCall` on `ImageProvider(Bitmap)` in `:feature:widget`, so a bitmap never returns | [ ] |
| B1 | Loading state for all four widgets: frame, header, Glance `CircularProgressIndicator` | [ ] |
| B2 | Text-first models in each mapper (`transformLatest`: text with placeholders, then images); a failed read logs and keeps the last model instead of throwing into the session | [ ] |
| B3 | Trending: prefetch the previous / next page's cover files after the shown page renders | [ ] |
| C1 | Random pick: enabled-list filter in the pool, the source chips and the settings screen; `SourceUnavailable` content with explanation copy; refresh on enabled-ids change | [ ] |
| D1 | Quote: `SizeMode.Exact`; font size and line count fitted to the real size by `StaticLayout` measurement (18sp down to a floor), replacing the three height tiers | [ ] |
| V | Minified build on Medium_Phone: rapid paging / shuffling / app switching, logcat free of `GlanceAppWidget` errors; then the user checks the Xiaomi 15T | [ ] |

A2, A3 and A4 touch disjoint widget files and run as parallel implementers after A1. B1–B3 follow A, since
they rework the same mappers and models. C1 is independent and can run beside A2–A4.

## Per-step routine

1. Implementer brief (Goal / Files / Verify / Report); A1 lands the shared DI and manifest wiring.
2. Build, then `softcover-test-writer` for the new logic (store naming / pruning / provider path checks,
   mapper emission order, pool and chip filtering), then `styleCheck`, then `softcover-reviewer`.
3. Install the minified variant on Medium_Phone and repeat the reproduction; stop for the user's go-ahead.

## Risks

- The launcher must open our provider. Pixel's launcher does; HyperOS can only be checked on the device (V).
- Tinted masks by `Uri` rely on `ImageView.setColorFilter`; check on API 26 during A3.
- A file pruned while a widget still shows it renders blank until the next update; the 7-day window and the
  daily maintenance update keep live files touched.
- Whether the Owned list's id appears in the enabled-ids set; check in C1 before filtering.
