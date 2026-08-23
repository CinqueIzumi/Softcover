package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
// region About content
/**
 * The About screen body, shared by the mobile [AboutScreen] page, the desktop standalone fallback, and
 * the desktop Settings master–detail pane's `About` category. Opens with an unheaded italic intro line
 * (the page title itself is chrome the caller already renders — a top bar on mobile, a static header on
 * desktop), then three [EditorialSectionHeader] groups — **Credits**, **Source**, **Contact** — each a
 * flat, hairline-separated stack of [AboutLinkRow] / [AboutUsernameRow] / [AboutNavigationRow] (the
 * Source section's `Roadmap` row, [onRoadmapClick]) — and closes with `VersionFooter`. About is the
 * app's **one and only** place the version shows: the mobile Settings menu and the desktop sidebar both
 * dropped their own copies specifically so it isn't visible in two places at once, so this is the single
 * call site for it, unconditionally, rather than a per-host toggle. [onComponentGalleryUnlocked] is
 * threaded straight through to that `VersionFooter` (its `onSecretUnlocked`) — see that composable's
 * KDoc for the tap gesture itself.
 */
@Composable
internal fun AboutContent(
    versionName: String,
    versionCode: Int,
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

        CreditsSection(openUrl = openUrl)

        Spacer(modifier = Modifier.height(40.dp))

        SourceSection(
            openUrl = openUrl,
            onRoadmapClick = onRoadmapClick,
        )

        Spacer(modifier = Modifier.height(40.dp))

        ContactSection(openUrl = openUrl)

        Spacer(modifier = Modifier.height(40.dp))

        VersionFooter(
            versionName = versionName,
            versionCode = versionCode,
            onSecretUnlocked = onComponentGalleryUnlocked,
        )
    }
}

/**
 * Where Softcover's book data comes from, and the disclaimer that has to sit beside it — Hardcover.app
 * neither runs nor endorses this client.
 */
@Composable
private fun CreditsSection(openUrl: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Credits",
            headline = "Powered by Hardcover",
        )

        Spacer(modifier = Modifier.height(20.dp))

        AboutLinkRow(
            title = "Hardcover.app",
            gloss = "Every book, cover and rating comes from its API.",
            icon = SoftcoverIcon.MenuBook,
            url = HARDCOVER_URL,
            openUrl = openUrl,
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
    openUrl: (String) -> Unit,
    onRoadmapClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Source",
            headline = "Built in the open",
        )

        Spacer(modifier = Modifier.height(20.dp))

        AboutLinkRow(
            title = "View the code on GitHub",
            gloss = "Browse the source, file a pull request, or fork it.",
            icon = SoftcoverIcon.History,
            url = GITHUB_REPO_URL,
            openUrl = openUrl,
        )

        AboutNavigationRow(
            title = "Roadmap",
            gloss = "What we're building next, in the order we plan to ship it.",
            icon = SoftcoverIcon.Explore,
            onClick = onRoadmapClick,
        )
    }
}

/**
 * Two ways to reach a person (report a bug, message the developer directly on Discord) and one way to
 * reach the wider community — a real invite link, not a plain mention, once one exists.
 */
@Composable
private fun ContactSection(openUrl: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Contact",
            headline = "Say hello",
        )

        Spacer(modifier = Modifier.height(20.dp))

        AboutLinkRow(
            title = "Report an issue",
            gloss = "Found a bug, or something feels off? Tell us on GitHub.",
            icon = SoftcoverIcon.Info,
            url = GITHUB_ISSUES_URL,
            openUrl = openUrl,
        )

        AboutUsernameRow(
            title = DISCORD_USERNAME,
            gloss = "The developer's Discord username.",
        )

        AboutLinkRow(
            title = "Join the Hardcover Discord",
            gloss = "Softcover has its own channel in the official server.",
            icon = SoftcoverIcon.Account,
            url = DISCORD_INVITE_URL,
            openUrl = openUrl,
        )
    }
}
// endregion
// region Rows
/**
 * A leaf action row for the About screen: [SoftcoverIcon.OpenInNew] on a row that hands off to
 * [openUrl] with [url]. Shares [AboutRow]'s anatomy with [AboutUsernameRow] — see that composable for
 * the shape itself.
 */
@Composable
private fun AboutLinkRow(
    title: String,
    gloss: String,
    icon: SoftcoverIcon,
    url: String,
    openUrl: (String) -> Unit,
) {
    val leadingIcon = drawableIconResource(
        icon = icon,
        contentDescription = title,
    )

    AboutRow(
        title = title,
        gloss = gloss,
        icon = leadingIcon,
        onClick = { openUrl(url) },
        trailingContent = {
            val trailingIcon = drawableIconResource(
                icon = SoftcoverIcon.OpenInNew,
                contentDescription = "Opens in your browser",
            )

            Icon(
                painter = trailingIcon.getIconPainter(),
                contentDescription = trailingIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}

/**
 * A leaf navigation row for the About screen: [SoftcoverIcon.KeyboardArrowRight] on a row that hands
 * off to [onClick] — the internal-navigation sibling of [AboutLinkRow], which instead opens an
 * external [url] behind [SoftcoverIcon.OpenInNew]. Shares [AboutRow]'s anatomy; see that composable for
 * the shape itself.
 */
@Composable
private fun AboutNavigationRow(
    title: String,
    gloss: String,
    icon: SoftcoverIcon,
    onClick: () -> Unit,
) {
    val leadingIcon = drawableIconResource(
        icon = icon,
        contentDescription = title,
    )

    AboutRow(
        title = title,
        gloss = gloss,
        icon = leadingIcon,
        onClick = onClick,
        trailingContent = {
            val trailingIcon = drawableIconResource(
                icon = SoftcoverIcon.KeyboardArrowRight,
                contentDescription = "",
            )

            Icon(
                painter = trailingIcon.getIconPainter(),
                contentDescription = trailingIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}

/**
 * The Discord-username row: plain selectable text, not a tap-to-copy action. Compose Multiplatform
 * 1.11.0's clipboard-write path (`Clipboard.setClipEntry`, the non-deprecated replacement for
 * `ClipboardManager.setText`) takes a `ClipEntry` that is an `expect class` with no common-code
 * constructor for plain text — every platform builds one from a native type (desktop: AWT
 * `Transferable`; Android: `ClipData`), so writing one from `commonMain` would mean inventing a new
 * platform `expect`/`actual` seam for this one leaf row. [SelectionContainer] sidesteps that entirely:
 * long-press-to-select is native OS text selection on every platform and reaches the system clipboard
 * without Softcover touching it.
 */
@Composable
private fun AboutUsernameRow(
    title: String,
    gloss: String,
) {
    val leadingIcon = drawableIconResource(
        icon = SoftcoverIcon.ContentPaste,
        contentDescription = title,
    )

    AboutRow(
        title = title,
        gloss = gloss,
        icon = leadingIcon,
        onClick = null,
        titleSelectable = true,
        trailingContent = {},
    )
}

/**
 * The shared row shell behind [AboutLinkRow] and [AboutUsernameRow] — a generalisation of the "Settings
 * menu row" anatomy (§4 of the design doc) for a leaf action instead of a sub-page push: the same top
 * `outlineVariant` hairline (drawn on every row, so a run reads as one continuous list), 42dp
 * `surfaceContainerHigh` icon tile, and Inter `titleMedium` label over an italic Fraunces `bodySmall`
 * gloss — but the trailing chevron is replaced by a caller-supplied [trailingContent]. [onClick] is
 * nullable: `null` (the Discord-username row) skips the hand-cursor/press-scale interaction modifiers
 * entirely rather than wiring a no-op click, and [titleSelectable] wraps just the title in a
 * [SelectionContainer] for that same row. Shared `commonMain` because [AboutContent] is; mobile's
 * push-only `SettingsMenuRow` (main Settings menu) stays private and chevron-only, since it never needs
 * a different trailing accessory or a non-interactive row.
 */
@Composable
private fun AboutRow(
    title: String,
    gloss: String,
    icon: RhaydusIconResource,
    onClick: (() -> Unit)?,
    trailingContent: @Composable () -> Unit,
    titleSelectable: Boolean = false,
) {
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant

    val interactionModifier = if (onClick != null) {
        Modifier
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(interactionModifier)
            .drawBehind {
                drawLine(
                    color = hairlineColor,
                    start = Offset(
                        x = 0f,
                        y = 0f,
                    ),
                    end = Offset(
                        x = size.width,
                        y = 0f,
                    ),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(
                horizontal = 4.dp,
                vertical = 17.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            val titleText: @Composable () -> Unit = {
                Text(
                    text = title,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (titleSelectable) {
                SelectionContainer {
                    titleText()
                }
            } else {
                titleText()
            }

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = gloss,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        trailingContent()
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
