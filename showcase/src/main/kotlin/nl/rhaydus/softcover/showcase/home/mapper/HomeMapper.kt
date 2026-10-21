package nl.rhaydus.softcover.showcase.home.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.gallery.GalleryRegistry
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.showcase.home.model.FamilyRowUiModel
import nl.rhaydus.softcover.showcase.live.hasLiveScreen

private val LIVE_BADGE = BadgeUiModel(
    label = "Live",
    tone = BadgeTone.PrimaryContainer,
)

internal fun homeMasthead(): PageMastheadUiModel = PageMastheadUiModel(
    title = "Showcase",
    subtitle = PageMastheadSubtitle.Text("Every component, in any spine colour and any brightness."),
)

internal fun familyRows(): ImmutableList<FamilyRowUiModel> = GalleryFamily.entries
    .map { family ->
        val count = GalleryRegistry.entries.count { it.family == family }

        FamilyRowUiModel(
            family = family,
            label = family.label,
            countLabel = if (count == 1) "1 component" else "$count components",
            liveBadge = if (family.hasLiveScreen()) LIVE_BADGE else null,
        )
    }
    .toImmutableList()
