package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.layout.FlowRowExpansion
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.PillChip
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * The canonical surface for managing the user's own tags on a book (§3.5 modal sheet). The user picks
 * a category and names a tag — there is no search step, as the upsert reuses an existing tag of that
 * name or mints a new one server-side. Tags already on the book render below, grouped by category like
 * a contents page, each removable and with a per-tag spoiler flag. Every change is committed immediately
 * by the caller — the sheet is a dumb renderer that reports intents through its callbacks. The book
 * title and its displayed edition are render-only inputs (for the header's naming and mini jacket); the
 * TOAD contract carries neither, both live on the caller's `BookDetailUiState`.
 *
 * Only the top bar and header are pinned; the add block and the collection share **one** scroll
 * region below them. That is what lets the suggestion cloud reveal the user's entire vocabulary for
 * a category in a single tap: an unbounded cloud in a pinned block would squeeze the collection's
 * `weight(1f)` to nothing and clip the sheet, whereas here it simply lengthens the page. Keeping the
 * naming field in the same scroll region as its suggestions also keeps the two adjacent, which a
 * pinned field with a scrolling cloud would not.
 */
private val EDITABLE_CATEGORIES: List<TagCategory> = listOf(
    TagCategory.GENRE,
    TagCategory.MOOD,
    TagCategory.TAG,
    TagCategory.CONTENT_WARNING,
)

/**
 * The name-field hint per category. Deliberately not "Name a ${'$'}{label} tag" — that template
 * degenerates to "Name a Tag tag" for the plain Tag category.
 */
private val TagCategory.namingHint: String
    get() = when (this) {
        TagCategory.GENRE -> "Name a genre…"
        TagCategory.MOOD -> "Name a mood…"
        TagCategory.TAG -> "Name a tag…"
        TagCategory.CONTENT_WARNING -> "Name a content warning…"
        TagCategory.OTHER -> "Name a tag…"
    }

/** A tag's identity for grouping / keying / new-entry tracking — `UserTag` carries no stable id. */
private typealias TagKey = Pair<TagCategory, String>

private fun UserTag.asKey(): TagKey = category to name

/**
 * Tags grouped by category in the fixed Genre → Mood → Tag → Content warning order; empty groups are
 * omitted. Any tag whose category isn't one of those four (the API can hand back `OTHER` for a null or
 * unrecognized server category) folds into a trailing "Other" group instead of disappearing — the old
 * sheet rendered every tag unconditionally, and this keeps that total coverage.
 */
private fun List<UserTag>.groupedForCollection(): List<TagGroup> {
    val editableGroups = EDITABLE_CATEGORIES.mapNotNull { category ->
        val tagsInCategory = filter { it.category == category }

        tagsInCategory.takeIf { it.isNotEmpty() }?.let {
            TagGroup(
                category = category,
                tags = it,
            )
        }
    }

    val otherTags = filter { it.category !in EDITABLE_CATEGORIES }

    return if (otherTags.isEmpty()) {
        editableGroups
    } else {
        editableGroups + TagGroup(
            category = TagCategory.OTHER,
            tags = otherTags,
        )
    }
}

@Composable
internal fun TagEditorBottomSheet(
    bookTitle: String,
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    userTags: List<UserTag>,
    tagSuggestions: List<UserTag>,
    selectedCategory: TagCategory,
    draft: String,
    onCategorySelected: (TagCategory) -> Unit,
    onDraftChange: (String) -> Unit,
    onAddTag: (String, TagCategory) -> Unit,
    onSuggestionSelected: (UserTag) -> Unit,
    onRemoveTag: (UserTag) -> Unit,
    onToggleSpoiler: (UserTag) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current
        val groups = userTags.groupedForCollection()
        val newlyAddedKeys = rememberNewlyAddedTagKeys(tags = userTags)
        val commitDraft = {
            onAddTag(
                draft,
                selectedCategory,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
        ) {
            TagEditorTopBar(onDoneClick = dismiss)

            TagEditorHeader(
                bookTitle = bookTitle,
                edition = edition,
                defaultEdition = defaultEdition,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(state = rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                TagEditorAddBlock(
                    selectedCategory = selectedCategory,
                    draft = draft,
                    tagSuggestions = tagSuggestions,
                    onCategorySelected = onCategorySelected,
                    onDraftChange = onDraftChange,
                    onCommit = commitDraft,
                    onSuggestionSelected = onSuggestionSelected,
                    modifier = Modifier.fillMaxWidth(),
                )

                TagEditorCollection(
                    groups = groups,
                    newlyAddedKeys = newlyAddedKeys,
                    onToggleSpoiler = onToggleSpoiler,
                    onRemove = onRemoveTag,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * Tracks which tag keys were newly inserted into [tags] since the previous composition, so the just-added
 * chip can play its entry animation while the rest of the collection stays static. The snapshot is only
 * compared once a prior snapshot exists, so the sheet's initial tags never animate in on open — only a
 * tag added during this sheet's lifetime does. Mirrors the foundation `LazyItemMutationAnimator`'s
 * snapshot-via-`SideEffect` idiom, adapted for a plain `FlowRow` rather than a lazy list.
 */
@Composable
private fun rememberNewlyAddedTagKeys(tags: List<UserTag>): Set<TagKey> {
    val playMotion = playDecorativeMotion()
    val currentKeys = tags.map { it.asKey() }.toSet()
    val previousKeys = remember { mutableStateOf<Set<TagKey>?>(null) }

    val newlyAdded = previousKeys.value?.let { previous ->
        if (playMotion) currentKeys - previous else emptySet()
    }.orEmpty()

    SideEffect { previousKeys.value = currentKeys }

    return newlyAdded
}

@Composable
private fun TagEditorTopBar(
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 4.dp,
            ),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = "Done",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .pointerHandCursor()
                .pressScaleClickable(onClick = onDoneClick)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        )
    }
}

/**
 * The canonical sheet opening — accent bar, eyebrow, italic headline, italic description — composed
 * locally rather than through `EditorialSectionHeader`, which has no trailing slot for the book's mini
 * jacket and no way to tint a substring of its description. It shares that component's anatomy and type
 * roles, with spacing tuned to the redline rather than the generic component's defaults; the layout (a
 * trailing cover) and the mixed-color description are bespoke.
 */
@Composable
private fun TagEditorHeader(
    bookTitle: String,
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "YOUR TAGS",
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tag it your way.",
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tagEditorDescription(bookTitle = bookTitle),
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        EditionImage(
            edition = edition,
            defaultEdition = defaultEdition,
            isLoading = false,
            coverlessTitle = bookTitle,
            cornerRadius = 4.dp,
            elevation = 4.dp,
            shadowColor = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.width(52.dp),
        )
    }
}

@Composable
private fun tagEditorDescription(bookTitle: String): AnnotatedString {
    val titleColor = MaterialTheme.colorScheme.primary

    return remember(bookTitle, titleColor) {
        buildAnnotatedString {
            append("Your own genres, moods and notes on ")
            withStyle(SpanStyle(color = titleColor)) {
                append(bookTitle)
            }
            append(". Mark any as a spoiler to keep it off shared cards.")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditorAddBlock(
    selectedCategory: TagCategory,
    draft: String,
    tagSuggestions: List<UserTag>,
    onCategorySelected: (TagCategory) -> Unit,
    onDraftChange: (String) -> Unit,
    onCommit: () -> Unit,
    onSuggestionSelected: (UserTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EDITABLE_CATEGORIES.forEach { category ->
                PillChip(
                    label = category.label,
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TagNamingField(
            draft = draft,
            placeholder = selectedCategory.namingHint,
            onDraftChange = onDraftChange,
            onCommit = onCommit,
        )

        if (tagSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            // Keyed on the category so the cloud's expansion does not survive a category switch:
            // `ExpandableFlowRow` holds its revealed-line count internally, so without a fresh key per
            // category, switching from an expanded Genre into Mood would show Mood already
            // part-unfurled to the same depth, having never asked for it.
            key(selectedCategory) {
                ExpandableFlowRow(
                    expansion = FlowRowExpansion.Progressive(linesPerExpand = 3),
                    collapsible = true,
                ) {
                    tagSuggestions.forEach { tag ->
                        key(tag.category, tag.name) {
                            PillChip(
                                label = tag.name,
                                selected = false,
                                onClick = { onSuggestionSelected(tag) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The naming pill: a tone-on-tone `surfaceContainerHigh` field with a leading edit glyph and a
 * borderless inline field — reads as naming, not a form box. `EditorialSearchField` hardwires a search
 * glyph and a trailing clear button and has no leading-edit / trailing-commit slot, so this is composed
 * from the same primitives instead of bending that component.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TagNamingField(
    draft: String,
    placeholder: String,
    onDraftChange: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val canCommit = draft.trim().isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(
                start = 14.dp,
                end = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val editIcon = drawableIconResource(
            contentDescription = "",
            icon = SoftcoverIcon.Edit,
        )

        Icon(
            painter = editIcon.getIconPainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (draft.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }

            BasicTextField(
                value = draft,
                onValueChange = onDraftChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onCommit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AnimatedVisibility(
            visible = canCommit,
            enter = if (playMotion) {
                fadeIn(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()) +
                    scaleIn(
                        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        initialScale = 0.9f,
                    )
            } else {
                EnterTransition.None
            },
            exit = if (playMotion) {
                fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()) +
                    scaleOut(
                        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        targetScale = 0.9f,
                    )
            } else {
                ExitTransition.None
            },
        ) {
            AddPill(onClick = onCommit)
        }
    }
}

@Composable
private fun AddPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(percent = 50),
    ) {
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun TagEditorCollection(
    groups: List<TagGroup>,
    newlyAddedKeys: Set<TagKey>,
    onToggleSpoiler: (UserTag) -> Unit,
    onRemove: (UserTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (groups.isEmpty()) {
            TagEditorEmptyState(modifier = Modifier.fillMaxWidth())
        } else {
            groups.forEachIndexed { index, group ->
                TagGroupSection(
                    group = group,
                    newlyAddedKeys = newlyAddedKeys,
                    onToggleSpoiler = onToggleSpoiler,
                    onRemove = onRemove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (index == 0) 6.dp else 26.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagGroupSection(
    group: TagGroup,
    newlyAddedKeys: Set<TagKey>,
    onToggleSpoiler: (UserTag) -> Unit,
    onRemove: (UserTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = group.category.label.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = group.tags.size.toString(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.height(11.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            group.tags.forEach { tag ->
                key(tag.category, tag.name) {
                    TagChip(
                        tag = tag,
                        isNewlyAdded = tag.asKey() in newlyAddedKeys,
                        onToggleSpoiler = { onToggleSpoiler(tag) },
                        onRemove = { onRemove(tag) },
                    )
                }
            }
        }
    }
}

/**
 * A tag pill with a leading spoiler-toggle eye and a trailing remove ×, at opposite ends so the two
 * actions never crowd each other. Plays a brief fade + rise on the first composition where
 * [isNewlyAdded] is true — i.e. the render pass that just inserted this tag — and never re-triggers on
 * later recompositions of the same chip (its `key(category, name)` slot keeps the `remember` below alive).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TagChip(
    tag: UserTag,
    isNewlyAdded: Boolean,
    onToggleSpoiler: () -> Unit,
    onRemove: () -> Unit,
) {
    val playMotion = playDecorativeMotion()
    val density = LocalDensity.current

    val visibleState = remember {
        MutableTransitionState(initialState = (isNewlyAdded && playMotion).not())
    }

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = if (playMotion) {
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                slideInVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) {
                    with(density) { 3.dp.roundToPx() }
                }
        } else {
            EnterTransition.None
        },
        exit = ExitTransition.None,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(start = 9.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            SpoilerToggleIcon(
                spoiler = tag.spoiler,
                onClick = onToggleSpoiler,
            )

            TagChipName(
                name = tag.name,
                spoiler = tag.spoiler,
            )

            RemoveTagIcon(
                tagName = tag.name,
                onClick = onRemove,
            )
        }
    }
}

@Composable
private fun SpoilerToggleIcon(
    spoiler: Boolean,
    onClick: () -> Unit,
) {
    val description = if (spoiler) "Marked as spoiler — tap to unmark" else "Mark as spoiler"
    val icon = if (spoiler) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility
    val tint = if (spoiler) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    val resolvedIcon = drawableIconResource(
        contentDescription = description,
        icon = icon,
    )

    DesktopTooltip(text = description) {
        Icon(
            painter = resolvedIcon.getIconPainter(),
            contentDescription = resolvedIcon.contentDescription,
            tint = tint,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick)
                .padding(3.dp)
                .size(17.dp),
        )
    }
}

@Composable
private fun TagChipName(
    name: String,
    spoiler: Boolean,
) {
    val style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)

    if (spoiler) {
        Text(
            text = name,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.spoilerEditorHighlight)
                .padding(horizontal = 3.dp, vertical = 1.dp),
        )
    } else {
        Text(
            text = name,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RemoveTagIcon(
    tagName: String,
    onClick: () -> Unit,
) {
    val description = "Remove $tagName"
    val resolvedIcon = drawableIconResource(
        contentDescription = description,
        icon = SoftcoverIcon.Close,
    )

    DesktopTooltip(text = description) {
        Icon(
            painter = resolvedIcon.getIconPainter(),
            contentDescription = resolvedIcon.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick)
                .padding(5.dp)
                .size(13.dp),
        )
    }
}

@Composable
private fun TagEditorEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(
            top = 40.dp,
            bottom = 30.dp,
            start = 10.dp,
            end = 10.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Nothing here yet.",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Name the first thing this book is to you.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
