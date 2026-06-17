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
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopTooltip
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

private enum class SettingsCategory {
    APPEARANCE,
    LIBRARY_TABS,
    ABOUT,
}

/**
 * Desktop renders the bespoke master–detail Settings surface.
 */
internal actual val settingsUsesMasterDetail: Boolean = true

/**
 * Desktop Settings: a category **source list** down the leading edge (the native desktop settings
 * idiom) beside a detail pane that swaps between Appearance and Library tabs **inline** — no push, no
 * full-page swap. Mirrors desktop Library's `[ sidebar | content ]` shape. "Your profile" is the one
 * entry that still pushes full-screen (Profile is a separate feature). The Appearance pane drives the
 * shared [settingsRunAction]; the Library-tabs pane drives [libraryVisibilityState] /
 * [libraryVisibilityRunAction] (its model is hosted under the Settings lifecycle in
 * [SettingsScreen.Content]) and docks a [LibraryVisibilitySaveBar] at the pane's bottom. The sub-page
 * `navigateTo*` callbacks are unused here (mobile pushes; desktop swaps) — only [navigateToProfile] is
 * wired.
 */
@Composable
internal actual fun SettingsScreenLayout(
    state: SettingsScreenUiState,
    settingsRunAction: (SettingsAction) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToLibraryVisibility: () -> Unit,
    libraryVisibilityState: LibraryVisibilitySettingsUiState,
    libraryVisibilityRunAction: (LibraryVisibilityAction) -> Unit,
    onCreateListClick: () -> Unit,
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
    debugSection: @Composable () -> Unit,
) {
    var selected by remember { mutableStateOf(SettingsCategory.APPEARANCE) }

    Row(modifier = Modifier.fillMaxSize()) {
        SettingsCategorySidebar(
            selected = selected,
            versionName = state.appVersionName,
            versionCode = state.appVersionCode,
            onSelect = { selected = it },
            onProfileClick = navigateToProfile,
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
                    debugSection = debugSection,
                )
            }
        }
    }
}
// region Sidebar
@Composable
private fun SettingsCategorySidebar(
    selected: SettingsCategory,
    versionName: String,
    versionCode: Int,
    onSelect: (SettingsCategory) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
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

            SidebarSectionLabel(text = "About")

            SettingsSidebarRow(
                label = "About",
                icon = SoftcoverIcon.Settings,
                selected = selected == SettingsCategory.ABOUT,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.ABOUT) },
            )
        }

        Text(
            text = "Version $versionName ($versionCode)",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = 26.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp,
            ),
        )
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
            val leadingIcon = SoftcoverIconResource.Drawable(
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
                val arrowIcon = SoftcoverIconResource.Drawable(
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
                .padding(vertical = 24.dp),
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

@Composable
private fun AboutPane(
    versionName: String,
    versionCode: Int,
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
    debugSection: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp),
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

                if (appUpdateState != AppUpdateState.Idle) {
                    AppUpdateSection(
                        appUpdateState = appUpdateState,
                        onClick = onStartAppUpdate,
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }

                debugSection()

                VersionFooter(
                    versionName = versionName,
                    versionCode = versionCode,
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
                val backIcon = SoftcoverIconResource.Drawable(
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
