package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.app.VersionComparator
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate
import nl.rhaydus.softcover.feature.settings.domain.model.ChangelogEntry
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentSection

internal fun ChangelogEntry.toDocumentSection(installedVersion: String): DocumentSection {
    val eyebrow = if (VersionComparator.isSameRelease(
            a = version,
            b = installedVersion,
        )) {
        "$version · Your version"
    } else {
        version
    }

    return DocumentSection(
        header = SectionHeaderUiModel.Section(
            eyebrow = eyebrow,
            headline = headline,
        ),
        dateText = releaseDate?.formatLongDate(),
        body = blocks.toMarkdownDocumentUiModel(),
    )
}
