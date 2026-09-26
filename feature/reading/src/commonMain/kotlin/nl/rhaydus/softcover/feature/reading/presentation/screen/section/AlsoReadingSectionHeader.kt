package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

/**
 * The "also reading" section opener: a tracked-caps eyebrow naming the count ("Three more in
 * motion") over the italic `headlineSmall` headline "Also between your fingers" — the two-tier
 * editorial section pattern (design-system.md §3.2), rather than [SectionLabel]'s single
 * accent+eyebrow line. Deliberately stepped down from the full `display` role, which read as
 * oversized for a body-section opener that isn't the page's own masthead.
 */
@Composable
internal fun AlsoReadingSectionHeader(count: Int) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(32.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = alsoReadingEyebrowText(count).uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Also between your fingers",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun alsoReadingEyebrowText(count: Int): String =
    if (count == 1) "One more in motion" else "$count more in motion"
