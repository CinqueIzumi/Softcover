package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Composable
internal fun AlsoReadingHeader(model: SectionHeaderUiModel) {
    Spacer(modifier = Modifier.height(32.dp))

    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionHeader(model = model)
    }

    Spacer(modifier = Modifier.height(8.dp))
}
