package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
internal fun AlsoReadingHeader(model: SectionHeaderUiModel) {
    Spacer(modifier = Modifier.height(Spacing.section))

    Box(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
        SectionHeader(model = model)
    }

    Spacer(modifier = Modifier.height(Spacing.md))
}
