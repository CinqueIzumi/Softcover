package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsMenuRow
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsMenuRowTarget
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsRowGroup
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
    versionLabel: String,
    creditsHeader: SectionHeaderUiModel,
    sourceHeader: SectionHeaderUiModel,
    contactHeader: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onWhatsNewClick: () -> Unit,
    onRoadmapClick: () -> Unit,
    onComponentGalleryUnlocked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SoftcoverText(
            text = "Softcover is an independent, open-source Hardcover.app client for Android, iOS " +
                "and desktop, built with Kotlin Multiplatform.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.xxxl))

        CreditsSection(
            header = creditsHeader,
            openUrl = openUrl,
        )

        Spacer(modifier = Modifier.height(Spacing.section))

        SourceSection(
            header = sourceHeader,
            openUrl = openUrl,
            onWhatsNewClick = onWhatsNewClick,
            onRoadmapClick = onRoadmapClick,
        )

        Spacer(modifier = Modifier.height(Spacing.section))

        ContactSection(
            header = contactHeader,
            openUrl = openUrl,
        )

        Spacer(modifier = Modifier.height(Spacing.section))

        VersionFooter(
            versionLabel = versionLabel,
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

        Spacer(modifier = Modifier.height(Spacing.md))

        SettingsRowGroup {
            row {
                SettingsMenuRow(
                    title = "Hardcover.app",
                    gloss = "Every book, cover and rating comes from its API.",
                    icon = SoftcoverIcon.MenuBook,
                    target = SettingsMenuRowTarget.ExternalLink(onClick = { openUrl(HARDCOVER_URL) }),
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        SoftcoverText(
            text = "This project is an independent, open-source application and is not affiliated " +
                "with, endorsed by, or sponsored by Hardcover.app.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SourceSection(
    header: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onWhatsNewClick: () -> Unit,
    onRoadmapClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        SettingsRowGroup {
            row {
                SettingsMenuRow(
                    title = "View the code on GitHub",
                    gloss = "Read the code, or help build it.",
                    icon = SoftcoverIcon.History,
                    target = SettingsMenuRowTarget.ExternalLink(onClick = { openUrl(GITHUB_REPO_URL) }),
                )
            }
            row {
                SettingsMenuRow(
                    title = "What's new",
                    gloss = "What changed in each version.",
                    icon = SoftcoverIcon.LibraryBooks,
                    target = SettingsMenuRowTarget.SubPage(onClick = onWhatsNewClick),
                )
            }
            row {
                SettingsMenuRow(
                    title = "Roadmap",
                    gloss = "What's coming next, in the order it's planned.",
                    icon = SoftcoverIcon.Explore,
                    target = SettingsMenuRowTarget.SubPage(onClick = onRoadmapClick),
                )
            }
        }
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

        Spacer(modifier = Modifier.height(Spacing.md))

        SettingsRowGroup {
            row {
                SettingsMenuRow(
                    title = "Report an issue",
                    gloss = "Found a bug? Let us know on GitHub.",
                    icon = SoftcoverIcon.Info,
                    target = SettingsMenuRowTarget.ExternalLink(onClick = { openUrl(GITHUB_ISSUES_URL) }),
                )
            }
            row {
                SettingsMenuRow(
                    title = DISCORD_USERNAME,
                    gloss = "The developer's Discord username.",
                    icon = SoftcoverIcon.ContentPaste,
                    target = SettingsMenuRowTarget.Copyable,
                )
            }
            row {
                SettingsMenuRow(
                    title = "Join the Hardcover Discord",
                    gloss = "Softcover has its own channel in the official server.",
                    icon = SoftcoverIcon.Account,
                    target = SettingsMenuRowTarget.ExternalLink(onClick = { openUrl(DISCORD_INVITE_URL) }),
                )
            }
        }
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
