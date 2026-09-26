package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(32.dp)
                .background(MaterialTheme.colorScheme.primary),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
