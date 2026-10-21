package nl.rhaydus.softcover.showcase.family.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.gallery.GalleryRegistry
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.family.model.EntrySectionUiModel
import nl.rhaydus.softcover.showcase.family.model.FixtureFrameUiModel

internal fun familyTopBar(family: GalleryFamily): TopBarUiModel = TopBarUiModel(title = family.label)

internal fun entrySections(family: GalleryFamily): ImmutableList<EntrySectionUiModel> = GalleryRegistry.entries
    .filter { it.family == family }
    .map { entry ->
        EntrySectionUiModel(
            name = entry.name,
            blurb = entry.blurb,
            fixtures = entry.fixtures
                .map {
                    FixtureFrameUiModel(
                        label = it.label.uppercase(),
                        fixture = it,
                    )
                }
                .toImmutableList(),
        )
    }
    .toImmutableList()
