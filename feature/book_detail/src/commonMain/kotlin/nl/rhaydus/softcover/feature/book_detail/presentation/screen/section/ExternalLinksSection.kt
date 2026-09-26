package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnExternalLinkClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "Find it" section (The Book lens): labeled outline pills — icon + label, hairline `outline`
 * border, `999` radius — replacing the earlier icon-only strip (design-system.md §5 external-links
 * pattern). Same UiEvent-based handoff, same ISBN gating, same verbose content descriptions.
 */
@Composable
internal fun ExternalLinksSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val isbn = state.displayedEdition?.let { edition ->
        edition.isbn13?.takeIf { it.isNotBlank() } ?: edition.isbn10?.takeIf { it.isNotBlank() }
    }

    // The ISBN is unknown until the edition resolves, almost always after t0 — so, like Tags, this
    // section is condition-gated rather than skeleton-reserved, but its arrival still animates in
    // (§5 "Book-detail lens-section reveal") instead of popping and shoving Voices down.
    // `AnimatedVisibility` must stay composed across the null→non-null flip for the enter transition
    // to play at all, so this reads `isbn` inside the content slot rather than guard-returning early.
    val playMotion = playDecorativeMotion()

    AnimatedVisibility(
        visible = isbn != null,
        enter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        isbn?.let { knownIsbn ->
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(36.dp))

                SmallSectionLabel(text = "Find it")

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ExternalLinkPill(
                        label = "Bookshop",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.Storefront,
                            contentDescription = "Find on Bookshop.org",
                        ),
                        onClick = {
                            runAction(
                                OnExternalLinkClickAction(
                                    url = "https://bookshop.org/search?keywords=$knownIsbn",
                                ),
                            )
                        },
                    )

                    ExternalLinkPill(
                        label = "Amazon",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.ShoppingBag,
                            contentDescription = "Find on Amazon",
                        ),
                        onClick = {
                            runAction(
                                OnExternalLinkClickAction(
                                    url = "https://www.amazon.com/s?k=$knownIsbn",
                                ),
                            )
                        },
                    )

                    ExternalLinkPill(
                        label = "OpenLibrary",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.LibraryBooks,
                            contentDescription = "Find on OpenLibrary",
                        ),
                        onClick = {
                            runAction(
                                OnExternalLinkClickAction(
                                    url = "https://openlibrary.org/search?isbn=$knownIsbn",
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExternalLinkPill(
    label: String,
    iconRes: RhaydusIconResource,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = iconRes.getIconPainter(),
                contentDescription = iconRes.contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}
