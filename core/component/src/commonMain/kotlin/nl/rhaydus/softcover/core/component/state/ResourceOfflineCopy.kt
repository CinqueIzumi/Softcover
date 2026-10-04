package nl.rhaydus.softcover.core.component.state

import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.BannerVariant
import nl.rhaydus.softcover.core.component.generated.resources.Res
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_banner
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_screen_body
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_screen_title
import org.jetbrains.compose.resources.getString

class ResourceOfflineCopy : OfflineCopy {
    override suspend fun emptyState(): EmptyStateUiModel = EmptyStateUiModel(
        title = getString(Res.string.connectivity_offline_screen_title),
        body = getString(Res.string.connectivity_offline_screen_body),
    )

    override suspend fun banner(): BannerUiModel = BannerUiModel(
        message = getString(Res.string.connectivity_offline_banner),
        variant = BannerVariant.Status,
        visible = true,
    )
}
