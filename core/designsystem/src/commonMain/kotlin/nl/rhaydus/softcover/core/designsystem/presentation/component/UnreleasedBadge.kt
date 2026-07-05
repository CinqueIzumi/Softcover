package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import nl.rhaydus.common.currentLocalDate

fun LocalDate.formatCompactRelease(): String {
    val formatter = LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        day(Padding.NONE)
    }

    val base = formatter.format(this)
    val now = currentLocalDate()

    return if (year == now.year) base else "$base, $year"
}

fun LocalDate.formatLongRelease(): String {
    val formatter = LocalDate.Format {
        monthName(MonthNames.ENGLISH_FULL)
        char(' ')
        day(Padding.NONE)
        char(',')
        char(' ')
        year()
    }

    return formatter.format(this)
}

@Composable
fun UnreleasedBadge(
    releaseDate: LocalDate,
    modifier: Modifier = Modifier,
    style: UnreleasedBadgeStyle = UnreleasedBadgeStyle.Compact,
) {
    val label = when (style) {
        UnreleasedBadgeStyle.Compact -> "Out ${releaseDate.formatCompactRelease()}"
        UnreleasedBadgeStyle.Prominent -> "Releases ${releaseDate.formatLongRelease()}"
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Unspecified,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

