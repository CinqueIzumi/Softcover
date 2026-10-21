package nl.rhaydus.softcover.showcase.live.callout.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.BannerVariant
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutVariant
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

internal fun calloutLiveTopBar(): TopBarUiModel = TopBarUiModel(title = GalleryFamily.CALLOUT.label)

internal fun liveBanners(): ImmutableList<BannerUiModel> = persistentListOf(
    BannerUiModel(
        message = "You're offline. Progress changes are saved and will sync when you reconnect.",
        variant = BannerVariant.Status,
        visible = true,
    ),
    BannerUiModel(
        message = "Read 24 pages today to stay on pace.",
        variant = BannerVariant.Editorial,
        visible = true,
    ),
)

internal fun liveCallouts(): ImmutableList<CalloutUiModel> = persistentListOf(
    CalloutUiModel(
        variant = CalloutVariant.Neutral(icon = SoftcoverIcon.Info),
        eyebrow = "On your shelves",
        body = "You're reading this one. Your progress and notes live under Yours.",
        dismissible = true,
    ),
    CalloutUiModel(
        variant = CalloutVariant.Neutral(),
        eyebrow = "Already on your shelves",
        body = "You've added a different edition of this book. Update it to the one you scanned?",
        callToAction = CalloutActionUiModel.Button(label = "Update edition"),
        dismissible = true,
    ),
    CalloutUiModel(
        variant = CalloutVariant.Highlight(title = "A new version is ready"),
        eyebrow = "Update available",
        body = "Fixes and a few small improvements to how your shelves sync.",
        callToAction = CalloutActionUiModel.Button(label = "Update now"),
        dismissible = true,
    ),
)
