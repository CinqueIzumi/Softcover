package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnNameChangedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnPrivacyToggledAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnSubmitAction
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState

/** A real title rather than "List name" — it shows the reader what a good list name looks like. */
private const val HERO_PLACEHOLDER = "Summer reads"

/** The floor the hero may shrink to before a very long name is allowed to scroll instead. */
private val HERO_MIN_FONT_SIZE = 22.sp

/** Width held back from the fit so the trailing caret never clips at the edge. */
private val HERO_CARET_RESERVE = 12.dp

/**
 * The "List Creation 1a" title page: a centred spread — eyebrow, a hairline rule broken by a quote
 * glyph, the name huge and borderless, a tappable public/private sentence, and a single filled
 * "Create list" pill. No field chrome anywhere; naming a list is a ten-second act of authorship, not
 * a form. This is a **documented exception** to the standard modal-sheet header (design-system.md §3.5
 * / §5 "Editorial section") — see the design-system doc's "Title-page sheet composition" pattern entry.
 */
@Composable
internal fun CreateListSheetContent(
    state: CreateListUiState,
    runAction: (CreateListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val submit: () -> Unit = {
        if (state.canSubmit) {
            keyboardController?.hide()

            runAction(OnSubmitAction())
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                start = 28.dp,
                end = 28.dp,
                bottom = 28.dp,
            ),
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "A NEW LIST",
            style = MaterialTheme.editorialTypography.eyebrow.copy(letterSpacing = 3.sp),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        QuoteRule()

        Spacer(modifier = Modifier.height(30.dp))

        NameHeroField(
            name = state.name,
            enabled = state.isSubmitting.not(),
            focusRequester = focusRequester,
            onNameChange = { runAction(OnNameChangedAction(newName = it)) },
            onSubmit = submit,
        )

        Spacer(modifier = Modifier.height(30.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 40.dp),
        )

        Spacer(modifier = Modifier.height(30.dp))

        PrivacyProseToggle(
            privacy = state.privacy,
            onToggle = { runAction(OnPrivacyToggledAction()) },
        )

        Spacer(modifier = Modifier.height(34.dp))

        RhaydusButton(
            label = "Create list",
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            enabled = state.canSubmit,
            onClick = submit,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
        )
    }
}

/**
 * The full-width hairline broken in the centre by a low-alpha quote glyph — replaces the usual
 * accent-bar-led eyebrow for this sheet's title-page composition. The glyph's `lineHeight` is pinned
 * to its `fontSize` (rather than inheriting `quoteGlyph`'s display-scale line box) so the rule stays a
 * thin row instead of reserving a whole display line; the residual baseline offset is corrected with
 * the `y` nudge below.
 */
@Composable
private fun QuoteRule(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph.copy(
                fontSize = 40.sp,
                lineHeight = 40.sp,
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            modifier = Modifier.offset(y = 6.dp),
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * The sheet's protagonist: a borderless, label-less name input rendered at display scale. The
 * platform text cursor (tinted `primary` via [SolidColor]) stands in for the spec's blinking caret —
 * a real cursor already blinks on every platform, so there is nothing to hand-animate. The
 * "Summer reads" placeholder is a real title, not an instructional label, and is drawn beneath the
 * field rather than through `BasicTextField`'s own decoration so it can carry the exact hero style.
 */
@Composable
private fun NameHeroField(
    name: String,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heroStyle = MaterialTheme.editorialTypography.display

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        // The hero is "sized to its own characters": the name sets the type size rather than the type
        // size clipping the name. Measured against the width actually available, so a long name steps
        // down instead of wrapping — the field is single-line, and a wrapping placeholder over a
        // single-line field is what put the caret in the middle of "Summer reads".
        val fittedStyle = heroStyle.copy(
            fontSize = fittedHeroFontSize(
                text = name.ifEmpty { HERO_PLACEHOLDER },
                style = heroStyle,
                availableWidth = maxWidth,
            ),
            textAlign = TextAlign.Center,
        )

        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            enabled = enabled,
            singleLine = true,
            textStyle = fittedStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            // The placeholder is laid out *beside* the field rather than stacked behind it: centring
            // the two on top of each other puts the caret in the middle of the placeholder word.
            //
            // The field leads and the placeholder follows, so the caret sits at the *start* of the
            // example name. The placeholder is a hint the first keystroke replaces wholesale, so a
            // caret trailing it would promise the opposite — that typing appends to "Summer reads".
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Bounded by what the placeholder leaves, but free to shrink to its content: at
                    // zero width (empty field) this is the caret alone, flush against the hint.
                    Box(
                        modifier = Modifier.weight(
                            weight = 1f,
                            fill = false,
                        ),
                    ) {
                        innerTextField()
                    }

                    if (name.isEmpty()) {
                        Text(
                            text = HERO_PLACEHOLDER,
                            style = fittedStyle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
    }
}

/**
 * The largest size in the hero's range at which [text] still fits [availableWidth] on one line,
 * stepping down 1sp at a time from the role's own size. [HERO_CARET_RESERVE] keeps room for the
 * trailing caret so it never clips off the edge at the moment the field fills up.
 */
@Composable
private fun fittedHeroFontSize(
    text: String,
    style: TextStyle,
    availableWidth: Dp,
): TextUnit {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    return remember(text, style, availableWidth, density) {
        val availablePx = with(density) { (availableWidth - HERO_CARET_RESERVE).toPx() }

        var candidate = style.fontSize
        while (candidate > HERO_MIN_FONT_SIZE) {
            val measured = textMeasurer.measure(
                text = AnnotatedString(text = text),
                style = style.copy(fontSize = candidate),
                maxLines = 1,
                softWrap = false,
            )

            if (measured.size.width <= availablePx) break

            candidate = (candidate.value - 1f).sp
        }

        candidate
    }
}

/**
 * "This shelf is *public* / *private*." — the privacy sentence you tap to flip. Each word is its own
 * tap target (§ "Prose toggle" pattern in design-system.md); tapping the already-chosen word is a
 * no-op since [OnPrivacyToggledAction] only flips, and flipping the already-selected word would just
 * flip it away again.
 */
@Composable
private fun PrivacyProseToggle(
    privacy: PrivacySetting,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPublic = privacy != PrivacySetting.PRIVATE

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.selectableGroup(),
    ) {
        Text(
            text = "This shelf is ",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        PrivacyWord(
            word = "public",
            isSelected = isPublic,
            onSelect = onToggle,
        )

        Text(
            text = " / ",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        PrivacyWord(
            word = "private",
            isSelected = isPublic.not(),
            onSelect = onToggle,
        )

        Text(
            text = ".",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * One tappable word of the [PrivacyProseToggle] sentence. A 20sp word sits well below the 48dp
 * minimum touch target, so [Modifier.minimumInteractiveComponentSize] pads the tap area invisibly
 * rather than inflating the glyph itself. The underline is always laid out (transparent when
 * unchosen) so flipping never reflows the sentence, and `Modifier.width(IntrinsicSize.Min)` sizes it
 * to the word's own measured width rather than the row's.
 */
@Composable
private fun PrivacyWord(
    word: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wordColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }
    val underlineColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .pointerHandCursor()
            .pressScaleClickable(onClick = { if (isSelected.not()) onSelect() })
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Min),
        ) {
            Text(
                text = word,
                style = MaterialTheme.editorialTypography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = wordColor,
            )

            Spacer(modifier = Modifier.height(1.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(underlineColor),
            )
        }
    }
}
