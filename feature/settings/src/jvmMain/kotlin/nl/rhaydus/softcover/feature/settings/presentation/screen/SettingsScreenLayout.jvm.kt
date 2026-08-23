package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

private enum class SettingsCategory {
    APPEARANCE,
    LIBRARY_TABS,
    ABOUT,
    ROADMAP,
}

/**
 * Desktop renders the bespoke master–detail Settings surface.
 */
internal actual val settingsUsesMasterDetail: Boolean = true

/**
 * Desktop Settings: a category **source list** down the leading edge (the native desktop settings
 * idiom) beside a detail pane that swaps between Appearance, Library tabs, and About **inline** — no
 * push, no full-page swap. Mirrors desktop Library's `[ sidebar | content ]` shape. "Your profile" is
 * the one entry that still pushes full-screen (Profile is a separate feature). The Appearance pane
 * drives the shared [settingsRunAction]; the Library-tabs pane drives [libraryVisibilityState] /
 * [libraryVisibilityRunAction] (its model is hosted under the Settings lifecycle in
 * [SettingsScreen.Content]) and docks a [LibraryVisibilitySaveBar] at the pane's bottom; the About pane
 * drives [openUrl] and its own `Roadmap` row (which selects the `ROADMAP` category rather than pushing);
 * the Roadmap pane drives [roadmapState] / [roadmapRunAction] (its model is likewise hosted under the
 * Settings lifecycle). The sub-page `navigateTo*` callbacks (including [navigateToAbout] and
 * [navigateToRoadmap]) are unused here (mobile pushes; desktop swaps) — only [navigateToProfile],
 * [navigateToHiddenSuggestions], and [navigateToComponentGallery] are wired, the last one into the
 * `About` pane's own version-footer easter egg (`component-contract.md` § 7.5).
 */
@Composable
internal actual fun SettingsScreenLayout(
    state: SettingsScreenUiState,
    settingsRunAction: (SettingsAction) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToLibraryVisibility: () -> Unit,
    navigateToHiddenSuggestions: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToRoadmap: () -> Unit,
    navigateToComponentGallery: () -> Unit,
    libraryVisibilityState: LibraryVisibilitySettingsUiState,
    libraryVisibilityRunAction: (LibraryVisibilityAction) -> Unit,
    roadmapState: RoadmapUiState,
    roadmapRunAction: (RoadmapAction) -> Unit,
    onCreateListClick: () -> Unit,
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
    openUrl: (String) -> Unit,
    debugSection: @Composable () -> Unit,
) {
    var selected by remember { mutableStateOf(SettingsCategory.APPEARANCE) }

    Row(modifier = Modifier.fillMaxSize()) {
        SettingsCategorySidebar(
            selected = selected,
            onSelect = { selected = it },
            onProfileClick = navigateToProfile,
            onHiddenSuggestionsClick = navigateToHiddenSuggestions,
            modifier = Modifier
                .width(SETTINGS_SIDEBAR_WIDTH)
                .fillMaxHeight(),
        )

        VerticalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            when (selected) {
                SettingsCategory.APPEARANCE -> AppearancePane(
                    state = state,
                    runAction = settingsRunAction,
                )

                SettingsCategory.LIBRARY_TABS -> LibraryTabsPane(
                    state = libraryVisibilityState,
                    runAction = libraryVisibilityRunAction,
                    onCreateListClick = onCreateListClick,
                )

                SettingsCategory.ABOUT -> AboutPane(
                    versionName = state.appVersionName,
                    versionCode = state.appVersionCode,
                    appUpdateState = appUpdateState,
                    onStartAppUpdate = onStartAppUpdate,
                    openUrl = openUrl,
                    onRoadmapClick = { selected = SettingsCategory.ROADMAP },
                    onComponentGalleryUnlocked = navigateToComponentGallery,
                    debugSection = debugSection,
                )

                SettingsCategory.ROADMAP -> RoadmapPane(
                    state = roadmapState,
                    runAction = roadmapRunAction,
                    openUrl = openUrl,
                )
            }
        }
    }
}
// region Sidebar
/**
 * The category source list. Carries no version text of its own — the app version shows exactly once,
 * on the `About` pane (via [AboutContent]'s `VersionFooter`), not here alongside it.
 */
@Composable
private fun SettingsCategorySidebar(
    selected: SettingsCategory,
    onSelect: (SettingsCategory) -> Unit,
    onProfileClick: () -> Unit,
    onHiddenSuggestionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
        ) {
            SidebarHeader()

            Spacer(modifier = Modifier.height(20.dp))

            SidebarSectionLabel(text = "Account")

            SettingsSidebarRow(
                label = "Your profile",
                icon = SoftcoverIcon.Account,
                selected = false,
                showTrailingArrow = true,
                onClick = onProfileClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SidebarSectionLabel(text = "Personalise")

            SettingsSidebarRow(
                label = "Appearance",
                icon = SoftcoverIcon.Palette,
                selected = selected == SettingsCategory.APPEARANCE,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.APPEARANCE) },
            )

            SettingsSidebarRow(
                label = "Library tabs",
                icon = SoftcoverIcon.Shelf,
                selected = selected == SettingsCategory.LIBRARY_TABS,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.LIBRARY_TABS) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            SidebarSectionLabel(text = "Privacy")

            SettingsSidebarRow(
                label = "Hidden suggestions",
                icon = SoftcoverIcon.FilterList,
                selected = false,
                showTrailingArrow = true,
                onClick = onHiddenSuggestionsClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SidebarSectionLabel(text = "About")

            SettingsSidebarRow(
                label = "About",
                icon = SoftcoverIcon.Settings,
                selected = selected == SettingsCategory.ABOUT,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.ABOUT) },
            )

            SettingsSidebarRow(
                label = "Roadmap",
                icon = SoftcoverIcon.Explore,
                selected = selected == SettingsCategory.ROADMAP,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.ROADMAP) },
            )
        }
    }
}

@Composable
private fun SidebarHeader() {
    Column(modifier = Modifier.padding(start = 26.dp, end = 16.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tune Softcover to match how you read.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SidebarSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = 26.dp,
            top = 8.dp,
            bottom = 6.dp,
        ),
    )
}

@Composable
private fun SettingsSidebarRow(
    label: String,
    icon: SoftcoverIcon,
    selected: Boolean,
    showTrailingArrow: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent

    val content = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val interactionSource = remember { MutableInteractionSource() }
    val rowShape = RoundedCornerShape(10.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 2.dp,
            )
            .pointerHandCursor()
            .hoverHighlight(
                interactionSource = interactionSource,
                shape = rowShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        color = container,
        contentColor = content,
        shape = rowShape,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val leadingIcon = drawableIconResource(
                icon = icon,
                contentDescription = "",
            )

            Icon(
                painter = leadingIcon.getIconPainter(),
                contentDescription = leadingIcon.contentDescription,
                modifier = Modifier.size(20.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )

            if (showTrailingArrow) {
                val arrowIcon = drawableIconResource(
                    icon = SoftcoverIcon.KeyboardArrowRight,
                    contentDescription = "",
                )

                Icon(
                    painter = arrowIcon.getIconPainter(),
                    contentDescription = arrowIcon.contentDescription,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
// endregion
// region Detail panes
@Composable
private fun AppearancePane(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = 24.dp,
                    bottom = 24.dp + rememberBottomBarPadding(),
                ),
        ) {
            Column(
                modifier = Modifier
                    .cappedContentWidth()
                    .padding(horizontal = 32.dp),
            ) {
                DesktopPaneHeader(
                    eyebrow = "Personalise",
                    title = "Appearance",
                    subtitle = "Make Softcover yours.",
                )

                Spacer(modifier = Modifier.height(28.dp))

                AppearanceSettingsContent(
                    state = state,
                    runAction = runAction,
                    showBottomBarToggle = false,
                    showShelfSwipeToggle = false,
                    showUiScaleControl = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        DesktopVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun LibraryTabsPane(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onCreateListClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LibraryVisibilityContent(
                state = state,
                runAction = runAction,
                onCreateListClick = onCreateListClick,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .cappedContentWidth()
                    .padding(horizontal = 32.dp),
            )

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }

        LibraryVisibilitySaveBar(
            isDirty = state.isDirty,
            isSaving = state.isSaving,
            onSave = { runAction(OnSaveLibraryVisibilityAction()) },
        )
    }
}

/**
 * The master–detail `About` category: [AboutContent] (Credits/Source/Contact, closing with its own
 * `VersionFooter`), then the app-update card and the debug section. [AboutContent] is the app's one and
 * only place the version shows — the sidebar's own copy was dropped so it isn't on screen twice at once
 * alongside this pane — so this doesn't render a second, separate `VersionFooter` of its own.
 * [onComponentGalleryUnlocked] is threaded straight through to that `VersionFooter` (via
 * [AboutContent]'s own parameter of the same name) — see its KDoc for the seven-tap gesture itself.
 */
@Composable
private fun AboutPane(
    versionName: String,
    versionCode: Int,
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
    openUrl: (String) -> Unit,
    onRoadmapClick: () -> Unit,
    onComponentGalleryUnlocked: () -> Unit,
    debugSection: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = 24.dp,
                    bottom = 24.dp + rememberBottomBarPadding(),
                ),
        ) {
            Column(
                modifier = Modifier
                    .cappedContentWidth()
                    .padding(horizontal = 32.dp),
            ) {
                DesktopPaneHeader(
                    eyebrow = "About",
                    title = "About Softcover",
                    subtitle = null,
                )

                Spacer(modifier = Modifier.height(28.dp))

                AboutContent(
                    versionName = versionName,
                    versionCode = versionCode,
                    openUrl = openUrl,
                    onRoadmapClick = onRoadmapClick,
                    onComponentGalleryUnlocked = onComponentGalleryUnlocked,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (appUpdateState != AppUpdateState.Idle) {
                    AppUpdateSection(
                        appUpdateState = appUpdateState,
                        onClick = onStartAppUpdate,
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }

                debugSection()
            }
        }

        DesktopVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

/**
 * The master–detail `Roadmap` category: a [DesktopPaneHeader] over the shared [RoadmapContent],
 * following [AboutPane]'s shape. No pull-to-refresh here (a touch-only gesture, not a desktop one) — the
 * retry inside a [RoadmapUiState.roadmapError] banner is the desktop refresh path.
 */
@Composable
private fun RoadmapPane(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
    openUrl: (String) -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = 24.dp,
                    bottom = 24.dp + rememberBottomBarPadding(),
                ),
        ) {
            Column(
                modifier = Modifier
                    .cappedContentWidth()
                    .padding(horizontal = 32.dp),
            ) {
                DesktopPaneHeader(
                    eyebrow = "Roadmap",
                    title = "Roadmap",
                    subtitle = "What we're building next, and roughly when.",
                )

                Spacer(modifier = Modifier.height(28.dp))

                RoadmapContent(
                    state = state,
                    runAction = runAction,
                    openUrl = openUrl,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        DesktopVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
internal fun DesktopPaneHeader(
    eyebrow: String,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.editorialTypography.pageTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Static back bar for the standalone desktop sub-setting pages ([AppearanceSettingsScreenLayout],
 * [LibraryVisibilitySettingsScreenLayout]). Those pages are a fallback for a direct push — the
 * primary desktop entry to these settings is the [SettingsScreenLayout] master–detail pane.
 */
@Composable
internal fun DesktopSettingsBackBar(
    title: String,
    onNavigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesktopTooltip(text = "Back") {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.pointerHandCursor(),
            ) {
                val backIcon = drawableIconResource(
                    icon = SoftcoverIcon.ArrowBack,
                    contentDescription = "Navigate back",
                )

                Icon(
                    painter = backIcon.getIconPainter(),
                    contentDescription = backIcon.contentDescription,
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
// endregion
private val SETTINGS_SIDEBAR_WIDTH = 240.dp
