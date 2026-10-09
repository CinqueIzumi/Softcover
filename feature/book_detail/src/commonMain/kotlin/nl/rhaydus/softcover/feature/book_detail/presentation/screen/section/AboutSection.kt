package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.editorial.component.DropCapText
import nl.rhaydus.designsystem.util.htmlToAnnotatedString
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.richtext.isBlank
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.displayFontFamily
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

private const val ABOUT_HEADLINE_STAND_IN = "A short line that sums the book up"

private const val ABOUT_DESCRIPTION_STAND_IN =
    "A longer description of the book sits here, running over several lines so the block " +
        "reserves roughly the height the real blurb will take once it arrives. It carries on " +
        "for a few more sentences, enough to fill about five lines of body text on a phone " +
        "screen before the real description replaces it with the actual copy."

@Composable
internal fun AboutSection(state: BookDetailUiState) {
    val headline = state.book?.headline.orEmpty()
    val description = state.book?.description.orEmpty()
    val isLoading = state.loadingBookDetails && headline.isBlank() && description.isBlank()
    val descriptionStandIn = remember { AnnotatedString(ABOUT_DESCRIPTION_STAND_IN) }

    Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
        SectionHeader(model = state.aboutSectionHeader)

        Spacer(modifier = Modifier.height(Spacing.md))

        Column {
            if (isLoading || headline.isNotBlank()) {
                SoftcoverText(
                    text = headline,
                    isLoading = isLoading,
                    standIn = ABOUT_HEADLINE_STAND_IN,
                    style = MaterialTheme.editorialTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (isLoading || (headline.isNotBlank() && description.isNotBlank())) {
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            if (isLoading || description.isNotBlank()) {
                DropCapText(
                    text = if (isLoading) {
                        descriptionStandIn
                    } else {
                        htmlToAnnotatedString(html = description)
                    },
                    bodyStyle = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                    ),
                    bodyColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .skeletonMask(visible = isLoading),
                    dropCapFontFamily = displayFontFamily(),
                )
            } else if (headline.isBlank()) {
                SoftcoverText(
                    text = "No description for this book yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
