package nl.rhaydus.softcover.core.designsystem.presentation.component

import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.ui.common.toHoursMinutesSeconds
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun UpdateProgressBottomSheet(
    bookToUpdate: Book,
    selectedTab: ProgressSheetTab,
    onProgressTabClick: (ProgressSheetTab) -> Unit,
    onUpdatePercentageClick: (String) -> Unit,
    onUpdatePageProgressClick: (String) -> Unit,
    onUpdateTimeProgressClick: (String, String, String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        ProgressBottomSheetContent(
            book = bookToUpdate,
            progressSheetTab = selectedTab,
            onProgressTabClick = onProgressTabClick,
            onUpdatePercentageClick = onUpdatePercentageClick,
            onUpdatePageProgressClick = onUpdatePageProgressClick,
            onUpdateTimeProgressClick = onUpdateTimeProgressClick,
        )
    }
}

@Composable
private fun ProgressBottomSheetContent(
    progressSheetTab: ProgressSheetTab,
    book: Book,
    onProgressTabClick: (ProgressSheetTab) -> Unit,
    onUpdatePercentageClick: (String) -> Unit,
    onUpdatePageProgressClick: (String) -> Unit,
    onUpdateTimeProgressClick: (String, String, String) -> Unit = { _, _, _ -> },
) {
    val isAudiobook = book.currentEdition?.isAudiobook == true

    val visibleTabs = if (isAudiobook) {
        listOf(ProgressSheetTab.TIME, ProgressSheetTab.PERCENTAGE)
    } else {
        listOf(ProgressSheetTab.PAGE, ProgressSheetTab.PERCENTAGE)
    }

    val activeTab = if (progressSheetTab in visibleTabs) {
        progressSheetTab
    } else {
        visibleTabs.first()
    }

    val focusManager = LocalFocusManager.current

    // A 96dp-tall L button is the right phone thumb target; on the desktop panel it steps down to a
    // 56dp M pointer target. The form is published by the hosting AdaptiveModalSheet.
    val actionButtonSize = when (LocalModalSheetForm.current) {
        ModalSheetForm.PANEL -> ButtonSize.M
        ModalSheetForm.SHEET -> ButtonSize.L
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

        Spacer(modifier = Modifier.height(32.dp))

        when (activeTab) {
            ProgressSheetTab.PAGE -> {
                ProgressBottomSheetPageContent(
                    book = book,
                    buttonSize = actionButtonSize,
                    onUpdatePageProgressClick = onUpdatePageProgressClick,
                )
            }

            ProgressSheetTab.TIME -> {
                ProgressBottomSheetTimeContent(
                    book = book,
                    buttonSize = actionButtonSize,
                    onUpdateTimeProgressClick = onUpdateTimeProgressClick,
                )
            }

            ProgressSheetTab.PERCENTAGE -> {
                ProgressBottomSheetPercentageContent(
                    book = book,
                    buttonSize = actionButtonSize,
                    onUpdatePercentageClick = onUpdatePercentageClick,
                )
            }
        }
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

@Composable
private fun ColumnScope.ProgressBottomSheetPageContent(
    book: Book,
    buttonSize: ButtonSize,
    onUpdatePageProgressClick: (String) -> Unit,
) {
    val totalPages = book.currentEdition?.pages ?: book.defaultEdition?.pages ?: 0

    var number by remember {
        val currentPage = book.userBookRead?.currentPage ?: 0

        mutableStateOf(TextFieldValue(text = currentPage.toString()))
    }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val parsed = number.text.toIntOrNull() ?: 0

    val fraction = if (totalPages > 0) {
        (parsed.toFloat() / totalPages).coerceIn(
            minimumValue = 0f,
            maximumValue = 1f,
        )
    } else {
        0f
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
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

                val updatedNumber = min(
                    newNumber,
                    totalPages,
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
    }

    Spacer(modifier = Modifier.height(8.dp))

    EditorialSuffix(text = "of $totalPages pages")

    Spacer(modifier = Modifier.height(28.dp))

    EditorialProgressIndicator(fraction = fraction)

    Spacer(modifier = Modifier.height(28.dp))

    SoftcoverButton(
        label = "Update progress",
        onClick = {
            onUpdatePageProgressClick(number.text)
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
    onUpdatePercentageClick: (String) -> Unit,
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
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

    Spacer(modifier = Modifier.height(8.dp))

    EditorialSuffix(text = "of the way through")

    Spacer(modifier = Modifier.height(28.dp))

    EditorialProgressIndicator(fraction = fraction)

    Spacer(modifier = Modifier.height(28.dp))

    SoftcoverButton(
        label = "Update progress",
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = buttonSize,
        onClick = {
            onUpdatePercentageClick(number.text)
        },
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ColumnScope.ProgressBottomSheetTimeContent(
    book: Book,
    buttonSize: ButtonSize,
    onUpdateTimeProgressClick: (String, String, String) -> Unit,
) {
    val initial = (book.userBookRead?.currentSeconds ?: 0).toHoursMinutesSeconds()
    val totalSeconds = book.currentEdition?.audioSeconds ?: 0
    val totalHms = totalSeconds.toHoursMinutesSeconds()

    var hours by remember { mutableStateOf(TextFieldValue(text = initial.hours.toString())) }
    var minutes by remember { mutableStateOf(TextFieldValue(text = initial.minutes.toString())) }
    var seconds by remember { mutableStateOf(TextFieldValue(text = initial.seconds.toString())) }

    val currentSeconds = (hours.text.toIntOrNull() ?: 0) * 3600 +
        (minutes.text.toIntOrNull() ?: 0) * 60 +
        (seconds.text.toIntOrNull() ?: 0)

    val fraction = if (totalSeconds > 0) {
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

    Spacer(modifier = Modifier.height(28.dp))

    SoftcoverButton(
        label = "Update progress",
        onClick = {
            onUpdateTimeProgressClick(
                hours.text,
                minutes.text,
                seconds.text,
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
            onUpdatePageProgressClick = {},
            onProgressTabClick = {},
            onUpdatePercentageClick = {},
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
            onUpdatePageProgressClick = {},
            onProgressTabClick = {},
            onUpdatePercentageClick = {},
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
