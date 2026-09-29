package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsMenuRow
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsRowAccessory
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.VersionFooter
// region About content
/**
 * The About screen body, shared by the mobile [AboutScreen] page, the desktop standalone fallback, and
 * the desktop Settings master–detail pane's `About` category. Opens with an unheaded italic intro line
 * (the page title itself is chrome the caller already renders — a top bar on mobile, a static header on
 * desktop), then three [SectionHeader] groups — **Credits**, **Source**, **Contact** — each a
 * flat, hairline-separated stack of `SettingsMenuRow`s (the Source section's `Roadmap` row,
 * [onRoadmapClick]) — and closes with `VersionFooter`. About is the
 * app's **one and only** place the version shows: the mobile Settings menu and the desktop sidebar both
 * dropped their own copies specifically so it isn't visible in two places at once, so this is the single
 * call site for it, unconditionally, rather than a per-host toggle. [onComponentGalleryUnlocked] is
 * threaded straight through to that `VersionFooter` (its `onSecretUnlocked`) — see that composable's
 * KDoc for the tap gesture itself.
 */
@Composable
internal fun AboutContent(
    versionFooterDivider: DividerUiModel,
    creditsHeader: SectionHeaderUiModel,
    sourceHeader: SectionHeaderUiModel,
    contactHeader: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onRoadmapClick: () -> Unit,
    onComponentGalleryUnlocked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Softcover is an independent, open-source Hardcover.app client for Android, iOS " +
                "and desktop, built with Kotlin Multiplatform.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        CreditsSection(
            header = creditsHeader,
            openUrl = openUrl,
        )

        Spacer(modifier = Modifier.height(40.dp))

        SourceSection(
            header = sourceHeader,
            openUrl = openUrl,
            onRoadmapClick = onRoadmapClick,
        )

        Spacer(modifier = Modifier.height(40.dp))

        ContactSection(
            header = contactHeader,
            openUrl = openUrl,
        )

        Spacer(modifier = Modifier.height(40.dp))

        VersionFooter(
            divider = versionFooterDivider,
            onSecretUnlocked = onComponentGalleryUnlocked,
        )
    }
}

/**
 * Where Softcover's book data comes from, and the disclaimer that has to sit beside it — Hardcover.app
 * neither runs nor endorses this client.
 */
@Composable
private fun CreditsSection(
    header: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsMenuRow(
            title = "Hardcover.app",
            gloss = "Every book, cover and rating comes from its API.",
            icon = SoftcoverIcon.MenuBook,
            accessory = SettingsRowAccessory.External,
            onClick = { openUrl(HARDCOVER_URL) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This project is an independent, open-source application and is not affiliated " +
                "with, endorsed by, or sponsored by Hardcover.app.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun SourceSection(
    header: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onRoadmapClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsMenuRow(
            title = "View the code on GitHub",
            gloss = "Browse the source, file a pull request, or fork it.",
            icon = SoftcoverIcon.History,
            accessory = SettingsRowAccessory.External,
            onClick = { openUrl(GITHUB_REPO_URL) },
        )

        SettingsMenuRow(
            title = "Roadmap",
            gloss = "What we're building next, in the order we plan to ship it.",
            icon = SoftcoverIcon.Explore,
            accessory = SettingsRowAccessory.Chevron,
            onClick = onRoadmapClick,
        )
    }
}

/**
 * Two ways to reach a person (report a bug, message the developer directly on Discord) and one way to
 * reach the wider community — a real invite link, not a plain mention, once one exists.
 */
@Composable
private fun ContactSection(
    header: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsMenuRow(
            title = "Report an issue",
            gloss = "Found a bug, or something feels off? Tell us on GitHub.",
            icon = SoftcoverIcon.Info,
            accessory = SettingsRowAccessory.External,
            onClick = { openUrl(GITHUB_ISSUES_URL) },
        )

        SettingsMenuRow(
            title = DISCORD_USERNAME,
            gloss = "The developer's Discord username.",
            icon = SoftcoverIcon.ContentPaste,
            accessory = SettingsRowAccessory.None,
            onClick = null,
        )

        SettingsMenuRow(
            title = "Join the Hardcover Discord",
            gloss = "Softcover has its own channel in the official server.",
            icon = SoftcoverIcon.Account,
            accessory = SettingsRowAccessory.External,
            onClick = { openUrl(DISCORD_INVITE_URL) },
        )
    }
}
// endregion
// region Constants
private const val HARDCOVER_URL = "https://hardcover.app/"
private const val GITHUB_REPO_URL = "https://github.com/CinqueIzumi/Softcover"
private const val GITHUB_ISSUES_URL = "https://github.com/CinqueIzumi/Softcover/issues"
private const val DISCORD_INVITE_URL = "https://discord.gg/hardcover"
private const val DISCORD_USERNAME = "Yogweh"
// endregion
