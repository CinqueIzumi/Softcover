package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.editorial.component.DropCapText
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.designsystem.util.htmlToAnnotatedString
import nl.rhaydus.softcover.core.component.richtext.isBlank
import nl.rhaydus.softcover.core.designsystem.presentation.theme.displayFontFamily
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@Composable
internal fun AboutSection(state: BookDetailUiState) {
    val headline = state.book?.headline.orEmpty()
    val description = state.book?.description.orEmpty()

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionLabel(text = "About")

        Spacer(modifier = Modifier.height(12.dp))

        SkeletonCrossfade(
            isLoading = state.loadingBookDetails && headline.isBlank() && description.isBlank(),
            label = "AboutSection",
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer(isLoading = true),
                )
            } else {
                Column {
                    if (headline.isNotBlank()) {
                        Text(
                            text = headline,
                            style = MaterialTheme.editorialTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (headline.isNotBlank() && description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (description.isNotBlank()) {
                        DropCapText(
                            text = htmlToAnnotatedString(html = description),
                            bodyStyle = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp,
                            ),
                            bodyColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            dropCapFontFamily = displayFontFamily(),
                        )
                    } else if (headline.isBlank()) {
                        Text(
                            text = "No description for this book yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
