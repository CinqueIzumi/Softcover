package nl.rhaydus.softcover.feature.profile.presentation.screen.section

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

/** A rating reads to one decimal everywhere it appears — the tile and the ratings row agree. */
internal const val RATING_FRACTION_DIGITS = 1

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

/**
 * The accent-bar + eyebrow + italic headline pair every new "reading life" region opens with — the
 * same rhythm [SectionLabel] alone already gives the existing "Reading atlas" block above, extended
 * with the headline the redesign's new sections need.
 */
@Composable
internal fun SectionIntro(
    eyebrow: String,
    headline: String,
) {
    Column {
        SectionLabel(text = eyebrow)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = headline,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// Shared with `presentation/mapper/ReadingLifeShareCardMapper.kt`, hence internal rather than private.
internal const val PERCENTAGE_MULTIPLIER = 100
