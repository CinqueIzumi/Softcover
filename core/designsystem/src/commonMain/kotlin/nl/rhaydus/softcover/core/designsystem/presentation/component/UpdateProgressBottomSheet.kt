package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.editorial.component.HeroStatNumberField
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book

@Composable
fun UpdateProgressBottomSheet(
    bookToUpdate: Book,
    selectedTab: ProgressSheetTab,
    onProgressTabClick: (ProgressSheetTab) -> Unit,
    onUpdatePercentageClick: (percentage: String, actionAt: String?) -> Unit,
    onUpdatePageProgressClick: (page: String, actionAt: String?) -> Unit,
    onUpdateTimeProgressClick: (hours: String, minutes: String, seconds: String, actionAt: String?) -> Unit,
    onDismissRequest: () -> Unit,
    onMarkAsReadClick: (actionAt: String?) -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        ProgressBottomSheetContent(
            book = bookToUpdate,
            progressSheetTab = selectedTab,
            onProgressTabClick = onProgressTabClick,
            onUpdatePercentageClick = onUpdatePercentageClick,
            onUpdatePageProgressClick = onUpdatePageProgressClick,
            onUpdateTimeProgressClick = onUpdateTimeProgressClick,
            onMarkAsReadClick = onMarkAsReadClick,
        )
    }
}

@Composable
private fun ProgressBottomSheetContent(
    progressSheetTab: ProgressSheetTab,
    book: Book,
    onProgressTabClick: (ProgressSheetTab) -> Unit,
    onUpdatePercentageClick: (percentage: String, actionAt: String?) -> Unit,
    onUpdatePageProgressClick: (page: String, actionAt: String?) -> Unit,
    onUpdateTimeProgressClick: (hours: String, minutes: String, seconds: String, actionAt: String?) -> Unit =
        { _, _, _, _ -> },
    onMarkAsReadClick: (actionAt: String?) -> Unit = {},
) {
    val edition = book.currentEdition
    val isAudiobook = edition?.isAudiobook == true

    // Percentage entry needs a known total to convert a fraction into pages (or seconds); without one
    // it can only ever record 0, so it's offered only when that total is known. The primary unit —
    // time for an audiobook, pages otherwise — always stays, allowing free entry even with no total.
    val primaryTab = if (isAudiobook) ProgressSheetTab.TIME else ProgressSheetTab.PAGE

    val totalPages = edition?.pages ?: book.defaultEdition?.pages ?: 0
    val totalSeconds = edition?.audioSeconds ?: 0
    val hasKnownTotal = if (isAudiobook) totalSeconds > 0 else totalPages > 0

    val visibleTabs = if (hasKnownTotal) {
        listOf(primaryTab, ProgressSheetTab.PERCENTAGE)
    } else {
        listOf(primaryTab)
    }

    // A stored unit that isn't valid for this book (e.g. PAGE on an audiobook) falls back to the
    // primary tab, which is always the first visible one.
    val activeTab = if (progressSheetTab in visibleTabs) progressSheetTab else primaryTab

    val focusManager = LocalFocusManager.current

    // Shared across all three tabs (§ When did you read this?, design-system.md patterns) — whichever
    // tab's "Update progress" button fires carries this value. Null means "just now": the server stamps
    // the mutation with its own current time, reproducing today's behaviour exactly. Once the reader
    // backdates it, the picked instant travels with every tab's submit until cleared.
    var readAt by remember { mutableStateOf<LocalDateTime?>(null) }

    val actionAt = readAt?.toInstant(TimeZone.currentSystemDefault())?.toString()

    // A moderate 56dp M button on both forms — the sheet form previously used a 96dp L button, but
    // that read as oversized against the redline spec's ~52/48dp actions, so both the phone sheet
    // and the desktop panel now share the same size. Still read through LocalModalSheetForm (rather
    // than hardcoding M directly) so a future form-specific need has a seam to hook into.
    val actionButtonSize = when (LocalModalSheetForm.current) {
        ModalSheetForm.PANEL, ModalSheetForm.SHEET -> ButtonSize.M
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
    ) {
        EditorialHeader(book = book)

        Spacer(modifier = Modifier.height(32.dp))

        TabSwitcher(
            activeTab = activeTab,
            visibleTabs = visibleTabs,
            onProgressTabClick = onProgressTabClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        WhenReadRow(
            pickedDateTime = readAt,
            onPickedDateTimeChange = { readAt = it },
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (activeTab) {
            ProgressSheetTab.PAGE -> {
                ProgressBottomSheetPageContent(
                    book = book,
                    buttonSize = actionButtonSize,
                    actionAt = actionAt,
                    onUpdatePageProgressClick = onUpdatePageProgressClick,
                )
            }

            ProgressSheetTab.TIME -> {
                ProgressBottomSheetTimeContent(
                    book = book,
                    buttonSize = actionButtonSize,
                    actionAt = actionAt,
                    onUpdateTimeProgressClick = onUpdateTimeProgressClick,
                )
            }

            ProgressSheetTab.PERCENTAGE -> {
                ProgressBottomSheetPercentageContent(
                    book = book,
                    buttonSize = actionButtonSize,
                    actionAt = actionAt,
                    onUpdatePercentageClick = onUpdatePercentageClick,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RhaydusButton(
            label = "Mark as read",
            style = ButtonStyle.OUTLINED,
            size = actionButtonSize,
            icon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = "Mark as read icon",
            ),
            onClick = { onMarkAsReadClick(actionAt) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EditorialHeader(book: Book) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(width = 32.dp)
                .height(height = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(size = 2.dp),
                ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "READING PROGRESS",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabSwitcher(
    activeTab: ProgressSheetTab,
    visibleTabs: List<ProgressSheetTab>,
    onProgressTabClick: (ProgressSheetTab) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        visibleTabs.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = tab == activeTab,
                onClick = { onProgressTabClick(tab) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = visibleTabs.size,
                ),
                label = {
                    Text(
                        text = tab.tabName,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditorialProgressIndicator(fraction: Float) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        label = "progressFraction",
    )

    LinearWavyProgressIndicator(
        progress = { animatedFraction },
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 12.dp),
    )
}

/**
 * A −/+ stepper flanking a hero number field (design-system.md's Update-progress sheet steppers
 * pattern). 44dp circle, `surfaceContainerHigh` fill, primary glyph.
 */
@Composable
private fun StepperCircle(
    symbol: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(44.dp)
            // The visible "−"/"+" glyph would otherwise merge into the announced label alongside
            // the content description; clear it and re-declare only what a screen reader should
            // say — the description plus the button role/action Surface's own onClick provides.
            .clearAndSetSemantics {
                role = Role.Button
                this.contentDescription = contentDescription
                onClick(label = null) {
                    onClick()
                    true
                }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

/**
 * "When did you read this?" (design-system.md's Backdate-a-logged-action pattern, §5). A pill
 * defaulting to "Just now" — no backdating, so the submitted `actionAt` is null and the server
 * stamps the mutation with its own current time. Tapping it seeds the picker with the current
 * moment and expands an inline day + time-of-day editor below; a trailing clear glyph resets to
 * "Just now" and collapses. Selected state swaps to `secondaryContainer`, matching the shared
 * pill-chip anatomy (§4 `PillChip`) this reuses at hand-rolled scale for its icon-leading form.
 */
@Composable
private fun WhenReadRow(
    pickedDateTime: LocalDateTime?,
    onPickedDateTimeChange: (LocalDateTime?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    // The sheet is a short-lived interaction (seconds, not minutes), so a single snapshot of "now"
    // taken at first composition is precise enough to anchor the "no future" clamp below — it does
    // not need to keep ticking while the picker is open.
    val now = remember { currentLocalDateTime() }
    val timeZone = remember { TimeZone.currentSystemDefault() }

    val isCustomized = pickedDateTime != null

    val containerColor = if (isCustomized) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (isCustomized) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(color = containerColor)
                .pointerHandCursor()
                .pressScaleClickable(
                    onClick = {
                        if (isCustomized.not()) {
                            onPickedDateTimeChange(now)
                        }

                        expanded = expanded.not()
                    },
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            val dateRangeIcon = drawableIconResource(
                icon = SoftcoverIcon.DateRange,
                contentDescription = "",
            )

            Icon(
                painter = dateRangeIcon.getIconPainter(),
                contentDescription = dateRangeIcon.contentDescription,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = pickedDateTime?.let {
                    formatWhenReadLabel(
                        picked = it,
                        now = now,
                    )
                } ?: "Just now",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )

            if (isCustomized) {
                Spacer(modifier = Modifier.width(8.dp))

                val clearIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Reset to just now",
                )

                Icon(
                    painter = clearIcon.getIconPainter(),
                    contentDescription = clearIcon.contentDescription,
                    tint = contentColor,
                    modifier = Modifier
                        .size(16.dp)
                        .pointerHandCursor()
                        .pressScaleClickable(
                            onClick = {
                                onPickedDateTimeChange(null)
                                expanded = false
                            },
                        ),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && isCustomized,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            WhenReadEditor(
                pickedDateTime = pickedDateTime ?: now,
                now = now,
                timeZone = timeZone,
                onPickedDateTimeChange = onPickedDateTimeChange,
            )
        }
    }
}

/** "Today" / "Yesterday" / "N days ago". */
private fun dayLabelFor(
    picked: LocalDateTime,
    now: LocalDateTime,
): String = when (val daysAgo = picked.date.daysUntil(now.date)) {
    0 -> "Today"
    1 -> "Yesterday"
    else -> "$daysAgo days ago"
}

/** [dayLabelFor], paired with the picked time-of-day. */
private fun formatWhenReadLabel(
    picked: LocalDateTime,
    now: LocalDateTime,
): String {
    val hh = picked.hour.toString().padStart(
        2,
        '0',
    )
    val mm = picked.minute.toString().padStart(
        2,
        '0',
    )

    return "${dayLabelFor(
        picked = picked,
        now = now,
    )}, $hh:$mm"
}

/**
 * The inline day + time-of-day editor beneath [WhenReadRow]'s pill. Every stepper shifts
 * [pickedDateTime] by a fixed [Duration] through instant arithmetic — which rolls a day/hour/minute
 * over correctly on its own — then clamps the result to [now] so the reader can never dial in a
 * future moment; a stepper simply stops moving once it reaches that boundary.
 */
@Composable
private fun WhenReadEditor(
    pickedDateTime: LocalDateTime,
    now: LocalDateTime,
    timeZone: TimeZone,
    onPickedDateTimeChange: (LocalDateTime) -> Unit,
) {
    fun shiftBy(duration: Duration) {
        val shifted = pickedDateTime
            .toInstant(timeZone)
            .plus(duration)
            .toLocalDateTime(timeZone)

        onPickedDateTimeChange(if (shifted > now) now else shifted)
    }

    val dayLabel = dayLabelFor(
        picked = pickedDateTime,
        now = now,
    )

    val timeStyle = MaterialTheme.editorialTypography.headlineMedium
    val hh = pickedDateTime.hour.toString().padStart(
        2,
        '0',
    )
    val mm = pickedDateTime.minute.toString().padStart(
        2,
        '0',
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperCircle(
                symbol = "−",
                contentDescription = "Earlier day",
                onClick = { shiftBy((-1).days) },
            )

            Spacer(modifier = Modifier.width(18.dp))

            Text(
                text = dayLabel,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(18.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later day",
                onClick = { shiftBy(1.days) },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperCircle(
                symbol = "−",
                contentDescription = "Earlier hour",
                onClick = { shiftBy((-1).hours) },
            )

            Spacer(modifier = Modifier.width(8.dp))

            StepperCircle(
                symbol = "−",
                contentDescription = "Earlier 15 minutes",
                onClick = { shiftBy((-15).minutes) },
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "$hh:$mm",
                style = timeStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(14.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later 15 minutes",
                onClick = { shiftBy(15.minutes) },
            )

            Spacer(modifier = Modifier.width(8.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later hour",
                onClick = { shiftBy(1.hours) },
            )
        }
    }
}

@Composable
private fun ColumnScope.ProgressBottomSheetPageContent(
    book: Book,
    buttonSize: ButtonSize,
    actionAt: String?,
    onUpdatePageProgressClick: (page: String, actionAt: String?) -> Unit,
) {
    val totalPages = book.currentEdition?.pages ?: book.defaultEdition?.pages ?: 0
    val hasTotal = totalPages > 0

    var number by remember {
        val currentPage = book.userBookRead?.currentPage ?: 0

        mutableStateOf(TextFieldValue(text = currentPage.toString()))
    }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val parsed = number.text.toIntOrNull() ?: 0

    val fraction = if (hasTotal) {
        (parsed.toFloat() / totalPages).coerceIn(
            minimumValue = 0f,
            maximumValue = 1f,
        )
    } else {
        0f
    }

    fun stepPageBy(delta: Int) {
        val next = (parsed + delta).let {
            if (hasTotal) {
                it.coerceIn(
                    0,
                    totalPages,
                )
            } else {
                it.coerceAtLeast(0)
            }
        }

        number = number.copy(
            text = next.toString(),
            selection = TextRange(next.toString().length),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperCircle(
            symbol = "−",
            contentDescription = "Decrease page",
            onClick = { stepPageBy(-1) },
        )

        Spacer(modifier = Modifier.width(18.dp))

        HeroStatNumberField(
            value = number,
            charCount = 4,
            onValueChange = { newValue ->
                if (firstTimeFocusedGained.not()) {
                    number = number.copy(selection = newValue.selection)
                } else {
                    firstTimeFocusedGained = false
                }

                if (newValue.text == number.text) return@HeroStatNumberField

                if (newValue.text.isEmpty()) {
                    number = newValue
                    return@HeroStatNumberField
                }

                val newNumber = newValue.text.toIntOrNull() ?: run {
                    number = number.copy(
                        text = "",
                        selection = newValue.selection,
                    )
                    return@HeroStatNumberField
                }

                val updatedNumber = if (hasTotal) {
                    min(
                        newNumber,
                        totalPages,
                    )
                } else {
                    newNumber
                }

                number = newValue.copy(text = updatedNumber.toString())
            },
            onFocusReset = {
                firstTimeFocusedGained = true
                number = number.copy(selection = TextRange.Zero)
            },
            onFocusGained = {
                number = number.copy(
                    selection = TextRange(
                        start = 0,
                        end = number.text.length,
                    ),
                )
            },
        )

        Spacer(modifier = Modifier.width(18.dp))

        StepperCircle(
            symbol = "+",
            contentDescription = "Increase page",
            onClick = { stepPageBy(1) },
        )
    }

    if (hasTotal) {
        Spacer(modifier = Modifier.height(8.dp))

        EditorialSuffix(text = "of $totalPages pages")

        Spacer(modifier = Modifier.height(28.dp))

        EditorialProgressIndicator(fraction = fraction)
    }

    Spacer(modifier = Modifier.height(28.dp))

    RhaydusButton(
        label = "Update progress",
        onClick = {
            onUpdatePageProgressClick(
                number.text,
                actionAt,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = buttonSize,
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ColumnScope.ProgressBottomSheetPercentageContent(
    book: Book,
    buttonSize: ButtonSize,
    actionAt: String?,
    onUpdatePercentageClick: (percentage: String, actionAt: String?) -> Unit,
) {
    var number by remember {
        val currentProgress = book.userBookRead?.progress?.roundToInt() ?: 0

        mutableStateOf(TextFieldValue(text = currentProgress.toString()))
    }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val parsed = number.text.toIntOrNull() ?: 0

    val fraction = (parsed.toFloat() / 100f).coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

    fun stepPercentageBy(delta: Int) {
        val next = (parsed + delta).coerceIn(
            0,
            100,
        )

        number = number.copy(
            text = next.toString(),
            selection = TextRange(next.toString().length),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperCircle(
            symbol = "−",
            contentDescription = "Decrease percentage",
            onClick = { stepPercentageBy(-1) },
        )

        Spacer(modifier = Modifier.width(18.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            HeroStatNumberField(
                value = number,
                charCount = 3,
                onValueChange = { newValue ->
                    if (firstTimeFocusedGained.not()) {
                        number = number.copy(selection = newValue.selection)
                    } else {
                        firstTimeFocusedGained = false
                    }

                    if (newValue.text == number.text) return@HeroStatNumberField

                    if (newValue.text.isEmpty()) {
                        number = newValue
                        return@HeroStatNumberField
                    }

                    val newNumber = newValue.text.toIntOrNull() ?: run {
                        number = number.copy(
                            text = "",
                            selection = newValue.selection,
                        )
                        return@HeroStatNumberField
                    }

                    val updatedNumber = min(
                        newNumber,
                        100,
                    )

                    number = newValue.copy(text = updatedNumber.toString())
                },
                onFocusReset = {
                    firstTimeFocusedGained = true
                    number = number.copy(selection = TextRange.Zero)
                },
                onFocusGained = {
                    number = number.copy(
                        selection = TextRange(
                            start = 0,
                            end = number.text.length,
                        ),
                    )
                },
            )

            Text(
                text = "%",
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        StepperCircle(
            symbol = "+",
            contentDescription = "Increase percentage",
            onClick = { stepPercentageBy(1) },
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    EditorialSuffix(text = "of the way through")

    Spacer(modifier = Modifier.height(28.dp))

    EditorialProgressIndicator(fraction = fraction)

    Spacer(modifier = Modifier.height(28.dp))

    RhaydusButton(
        label = "Update progress",
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = buttonSize,
        onClick = {
            onUpdatePercentageClick(
                number.text,
                actionAt,
            )
        },
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ColumnScope.ProgressBottomSheetTimeContent(
    book: Book,
    buttonSize: ButtonSize,
    actionAt: String?,
    onUpdateTimeProgressClick: (hours: String, minutes: String, seconds: String, actionAt: String?) -> Unit,
) {
    val initial = (book.userBookRead?.currentSeconds ?: 0).toHoursMinutesSeconds()
    val totalSeconds = book.currentEdition?.audioSeconds ?: 0
    val hasTotal = totalSeconds > 0
    val totalHms = totalSeconds.toHoursMinutesSeconds()

    var hours by remember { mutableStateOf(TextFieldValue(text = initial.hours.toString())) }
    var minutes by remember { mutableStateOf(TextFieldValue(text = initial.minutes.toString())) }
    var seconds by remember { mutableStateOf(TextFieldValue(text = initial.seconds.toString())) }

    val currentSeconds = (hours.text.toIntOrNull() ?: 0) * 3600 +
        (minutes.text.toIntOrNull() ?: 0) * 60 +
        (seconds.text.toIntOrNull() ?: 0)

    val fraction = if (hasTotal) {
        (currentSeconds.toFloat() / totalSeconds).coerceIn(
            minimumValue = 0f,
            maximumValue = 1f,
        )
    } else {
        0f
    }

    val timeStyle = MaterialTheme.editorialTypography.headlineMedium

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeField(
            value = hours,
            charCount = 2,
            textStyle = timeStyle,
            onValueChange = { newValue ->
                val parsed = newValue.text.toIntOrNull()
                hours = when {
                    newValue.text.isEmpty() -> newValue
                    parsed == null -> hours
                    else -> newValue.copy(text = parsed.coerceAtLeast(minimumValue = 0).toString())
                }
            },
        )

        TimeColon(textStyle = timeStyle)

        StepperCircle(
            symbol = "−",
            contentDescription = "Decrease minutes",
            onClick = {
                val next = ((minutes.text.toIntOrNull() ?: 0) - 1).coerceIn(
                    0,
                    59,
                )
                minutes = minutes.copy(text = next.toString())
            },
            modifier = Modifier.padding(end = 6.dp),
        )

        TimeField(
            value = minutes,
            charCount = 2,
            textStyle = timeStyle,
            onValueChange = { newValue ->
                val parsed = newValue.text.toIntOrNull()
                minutes = when {
                    newValue.text.isEmpty() -> newValue
                    parsed == null -> minutes
                    else -> newValue.copy(
                        text = parsed.coerceIn(
                            minimumValue = 0,
                            maximumValue = 59,
                        ).toString(),
                    )
                }
            },
        )

        StepperCircle(
            symbol = "+",
            contentDescription = "Increase minutes",
            onClick = {
                val next = ((minutes.text.toIntOrNull() ?: 0) + 1).coerceIn(
                    0,
                    59,
                )
                minutes = minutes.copy(text = next.toString())
            },
            modifier = Modifier.padding(start = 6.dp),
        )

        TimeColon(textStyle = timeStyle)

        TimeField(
            value = seconds,
            charCount = 2,
            textStyle = timeStyle,
            onValueChange = { newValue ->
                val parsed = newValue.text.toIntOrNull()
                seconds = when {
                    newValue.text.isEmpty() -> newValue
                    parsed == null -> seconds
                    else -> newValue.copy(
                        text = parsed.coerceIn(
                            minimumValue = 0,
                            maximumValue = 59,
                        ).toString(),
                    )
                }
            },
        )
    }

    if (hasTotal) {
        Spacer(modifier = Modifier.height(8.dp))

        EditorialSuffix(
            text = "of " +
                "${totalHms.hours.toString().padStart(
                    2,
                    '0',
                )}:" +
                "${totalHms.minutes.toString().padStart(
                    2,
                    '0',
                )}:" +
                totalHms.seconds.toString().padStart(
                    2,
                    '0',
                ),
        )

        Spacer(modifier = Modifier.height(28.dp))

        EditorialProgressIndicator(fraction = fraction)
    }

    Spacer(modifier = Modifier.height(28.dp))

    RhaydusButton(
        label = "Update progress",
        onClick = {
            onUpdateTimeProgressClick(
                hours.text,
                minutes.text,
                seconds.text,
                actionAt,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = buttonSize,
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun TimeColon(textStyle: TextStyle) {
    Text(
        text = ":",
        modifier = Modifier.padding(horizontal = 4.dp),
        style = textStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Approximate width for a hero stat field of [charCount] glyphs in [textStyle]. */
@Composable
private fun computeHeroStatFieldWidth(
    textStyle: TextStyle,
    charCount: Int,
): Dp {
    val density = LocalDensity.current

    return remember(density, textStyle, charCount) {
        with(density) {
            val fontSizeInPx = textStyle.fontSize.toPx()
            val padding = 16.dp.toPx()

            ((charCount * fontSizeInPx * 0.62f) + padding).toDp()
        }
    }
}

@Composable
private fun TimeField(
    value: TextFieldValue,
    charCount: Int,
    textStyle: TextStyle,
    onValueChange: (TextFieldValue) -> Unit,
) {
    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val width = computeHeroStatFieldWidth(
        textStyle = textStyle,
        charCount = charCount,
    )

    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (firstTimeFocusedGained) {
                firstTimeFocusedGained = false
                if (newValue.text == value.text) return@BasicTextField
            }

            onValueChange(newValue)
        },
        singleLine = true,
        textStyle = textStyle.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .width(width = width)
            .onFocusChanged { focusState ->
                if (focusState.hasFocus.not()) {
                    firstTimeFocusedGained = true
                    onValueChange(value.copy(selection = TextRange.Zero))
                    return@onFocusChanged
                }

                onValueChange(
                    value.copy(
                        selection = TextRange(
                            start = 0,
                            end = value.text.length,
                        ),
                    ),
                )
            },
    )
}

@StandardPreview
@Composable
private fun ProgressSheetContentPagePreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            onUpdatePageProgressClick = { _, _ -> },
            onProgressTabClick = {},
            onUpdatePercentageClick = { _, _ -> },
            progressSheetTab = ProgressSheetTab.PAGE,
            book = PreviewData.baseBook.copy(
                title = "The Dungeon Anarchist's Cookbook",
                editions = listOf(
                    PreviewData.baseEdition.copy(pages = 534),
                ),
                userBookRead = PreviewData.baseBook.userBookRead?.copy(currentPage = 80),
            ),
        )
    }
}

@StandardPreview
@Composable
private fun ProgressSheetContentPercentagePreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            onUpdatePageProgressClick = { _, _ -> },
            onProgressTabClick = {},
            onUpdatePercentageClick = { _, _ -> },
            progressSheetTab = ProgressSheetTab.PERCENTAGE,
            book = PreviewData.baseBook.copy(
                title = "The Dungeon Anarchist's Cookbook",
                editions = listOf(
                    PreviewData.baseEdition.copy(pages = 534),
                ),
                userBookRead = PreviewData.baseBook.userBookRead?.copy(
                    currentPage = 80,
                    progress = 35f,
                ),
            ),
        )
    }
}
