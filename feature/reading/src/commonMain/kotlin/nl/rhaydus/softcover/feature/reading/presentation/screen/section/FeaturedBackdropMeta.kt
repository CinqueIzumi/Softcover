package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.ReadingHeroBackdropForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import kotlin.math.roundToInt

/** The series eyebrow, hero title, byline, and "Your pace" row sitting over the dark backdrop. */
@Composable
internal fun FeaturedBackdropMeta(
    book: Book,
    mutationFailed: Boolean,
    paceForecast: ReadingPaceForecast?,
    modifier: Modifier = Modifier,
) {
    val overlayShadow = Shadow(
        color = Color.Black.copy(alpha = 0.85f),
        offset = Offset(
            x = 0f,
            y = 1f,
        ),
        blurRadius = 14f,
    )
    val foreground = ReadingHeroBackdropForeground

    Column(modifier = modifier) {
        val topLineText = if (mutationFailed) {
            "Couldn't save — tap to retry".uppercase()
        } else {
            book.seriesText?.takeIf { it.isNotBlank() }?.uppercase()
        }

        if (topLineText != null) {
            Text(
                text = topLineText,
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = overlayShadow,
                ),
                color = if (mutationFailed) MaterialTheme.colorScheme.error else foreground.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.headlineSmall.copy(
                lineHeight = 27.sp,
                shadow = overlayShadow,
            ),
            color = foreground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        book.currentEdition?.authorString?.takeIf { it.isNotBlank() }?.let { authors ->
            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "By $authors".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                    letterSpacing = 1.2.sp,
                    shadow = overlayShadow,
                ),
                color = foreground.copy(alpha = 0.74f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // The pace forecast is a separate network round trip that trails the rest of the card by a
        // beat, and may never resolve for a given book at all — so it does not reserve height; it
        // reveals in place with the app's standard vertical-reveal register instead of popping in and
        // shoving the deadline row/hero actions beneath it down in a single frame (design-system.md
        // §5 "Book-detail in-progress stat").
        val playMotion = playDecorativeMotion()
        val revealEnter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None
        val revealExit = if (playMotion) shrinkVertically() + fadeOut() else ExitTransition.None

        var lastPaceForecast by remember { mutableStateOf(paceForecast) }

        if (paceForecast != null) {
            lastPaceForecast = paceForecast
        }

        AnimatedVisibility(
            visible = paceForecast != null,
            enter = revealEnter,
            exit = revealExit,
        ) {
            lastPaceForecast?.let { forecast ->
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Your pace".uppercase(),
                            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                                letterSpacing = 1.4.sp,
                                shadow = overlayShadow,
                            ),
                            color = foreground.copy(alpha = 0.7f),
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(foreground.copy(alpha = 0.24f)),
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = paceLineText(forecast),
                        style = MaterialTheme.editorialTypography.bodySmall.copy(shadow = overlayShadow),
                        color = foreground.copy(alpha = 0.95f),
                    )
                }
            }
        }
    }
}

/** "35 pages a day — about 5 days to go" (or the audiobook time equivalent). */
private fun paceLineText(forecast: ReadingPaceForecast): String {
    val amount = when (forecast.unit) {
        DeadlineUnit.PAGES -> {
            val perDay = forecast.avgPerReadingDay.roundToInt()
            val pageLabel = if (perDay == 1) "page" else "pages"

            "$perDay $pageLabel a day"
        }

        DeadlineUnit.SECONDS -> "${secondsToHm(forecast.avgPerReadingDay.roundToInt())} a day"
    }

    val days = forecast.forecastReadingDays
    val daysLabel = if (days == 1) "day" else "days"

    return "$amount — about $days $daysLabel to go"
}
