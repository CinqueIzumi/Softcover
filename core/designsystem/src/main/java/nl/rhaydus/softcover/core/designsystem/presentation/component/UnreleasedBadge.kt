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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDate.formatCompactRelease(): String {
    val formatter = DateTimeFormatter.ofPattern(
        "MMM d",
        Locale.getDefault(),
    )

    val base = format(formatter)
    val now = LocalDate.now()

    return if (year == now.year) base else "$base, $year"
}

fun LocalDate.formatLongRelease(): String {
    val formatter = DateTimeFormatter.ofPattern(
        "MMMM d, yyyy",
        Locale.getDefault(),
    )

    return format(formatter)
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

