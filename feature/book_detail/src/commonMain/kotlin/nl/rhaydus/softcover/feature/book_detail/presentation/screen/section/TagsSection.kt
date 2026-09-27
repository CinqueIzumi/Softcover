package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.ChipVariant
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "Tags" section (The Book lens): the community tag block (grouped by category, top-5,
 * content-warning tags concealed via [ConcealableTagChip]'s spoiler reveal-in-place), followed by the
 * edition colophon line (publisher · format, year · ISBN-13) — folded into one section per the spec,
 * rather than two separate strips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagsSection(state: BookDetailUiState) {
    val groups = state.communityTagGroups

    val edition = state.displayedEdition
    val colophon = editionColophonLine(edition = edition)
    val hasContent = groups.isNotEmpty() || colophon != null

    // Community tags and the edition colophon are both unknown until the book/edition resolves
    // (rarely before, since neither travels on `initialCover`), so this section is entirely absent
    // through the loading phase — a bare content-dependent condition, exactly like the DS's
    // "Actionable inline banner" precedent, so no skeleton is reserved for it. What DOES need
    // fixing is the pop itself: appearing mid-column with no transition shoves Find it / Voices
    // down in a single frame, so the reveal rides the standard vertical-reveal register instead.
    val playMotion = playDecorativeMotion()

    AnimatedVisibility(
        visible = hasContent,
        enter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(36.dp))

            if (groups.isNotEmpty()) {
                SmallSectionLabel(text = "Tags")
            }

            groups.forEach { group ->
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = group.category.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    group.chips.forEach { chip ->
                        key(chip.key) {
                            if (chip.variant == ChipVariant.Spoiler) {
                                ConcealableTagChip(model = chip)
                            } else {
                                Chip(model = chip)
                            }
                        }
                    }
                }
            }

            if (colophon != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = colophon,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** `{publisher} · {format}, {year} · ISBN-13 {isbn13}`, keeping only the parts that are known. */
private fun editionColophonLine(edition: BookEdition?): String? {
    if (edition == null) return null

    val publisher = edition.publisher?.takeIf { it.isNotBlank() }

    val formatAndYear = buildString {
        val format = edition.format.takeIf { it.isNotBlank() }
        val year = edition.releaseYear.takeIf { it != -1 && it > 0 }

        if (format != null) append(format)

        if (year != null) {
            if (format != null) append(", ")
            append(year)
        }
    }.takeIf { it.isNotBlank() }

    val isbn13 = edition.isbn13?.takeIf { it.isNotBlank() }?.let { "ISBN-13 $it" }

    val parts = listOfNotNull(publisher, formatAndYear, isbn13)

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
}

@Composable
private fun ConcealableTagChip(model: ChipUiModel) {
    var revealed by rememberSaveable { mutableStateOf(false) }

    if (revealed) {
        Chip(model = model.copy(variant = ChipVariant.Tonal()))
    } else {
        Chip(
            model = model,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .clickable(
                    onClickLabel = "Reveal content warning",
                    role = Role.Button,
                ) {
                    revealed = true
                },
        )
    }
}
