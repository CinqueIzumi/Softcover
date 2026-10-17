package nl.rhaydus.softcover.smoketest

import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

private typealias Finder = () -> UiObject2?

/**
 * Black-box, read-only journey through the installed minified app, signed in with the token passed as the
 * `hardcoverToken` instrumentation argument. Nothing is created, edited or deleted on the account: the
 * create-list sheet is opened and never submitted.
 */
class SignedInJourneyTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device: UiDevice = UiDevice.getInstance(instrumentation)
    private val context: Context = instrumentation.context

    @After
    fun restoreSystemSettings() {
        device.executeShellCommand("settings put global always_finish_activities 0")
    }

    /**
     * One test with named phases so the app is cleared and signed in once; the later phases reuse that
     * signed-in state.
     */
    @Test
    fun signedInJourney() {
        signInFresh()

        journeyThroughEveryTabBookDetailProcessDeathAndSettings()

        returnToTabs()

        surviveDontKeepActivitiesAcrossTabsBookDetailAndHome()
    }

    private fun signInFresh() {
        val token = InstrumentationRegistry.getArguments().getString(TOKEN_ARGUMENT)

        assertTrue(
            "No Hardcover token reached the smoke test. Set HARDCOVER_SMOKE_TOKEN (CI secret) or " +
                "hardcover.apiToken in local.properties so Gradle passes the $TOKEN_ARGUMENT instrumentation argument.",
            token.isNullOrBlank().not(),
        )

        device.executeShellCommand("pm clear $APP_PACKAGE")
        device.pressHome()
        launchApp()

        passOnboardingIntro()
        enterToken(token.orEmpty())

        val save = waitFor(
            finder = { enabledSaveButton() },
            what = "the enabled Save API key button",
        )

        save.click()
        device.waitForIdle()

        val bottomBar = labelled("^Reading$")
        val rejection = labelled(REJECTED_KEY_PATTERN)
        val setupFailure = labelled(SETUP_FAILED_PATTERN)

        waitFor(
            finder = {
                check(rejection() == null) { REJECTED_TOKEN_MESSAGE }
                check(setupFailure() == null) { SETUP_FAILED_MESSAGE }

                bottomBar()
            },
            timeoutMillis = SYNC_TIMEOUT_MILLIS,
            what = "the bottom bar after the initial sync",
        )
    }

    private fun journeyThroughEveryTabBookDetailProcessDeathAndSettings() {
        visitEveryTab()

        openBookFromLibrary()
        openFullScreenCover()
        backgroundKillAndRestore(
            expected = "More actions",
            what = "book detail after the restored cover viewer popped itself",
        )
        returnToTabs()

        openCreateListSheet()
        backgroundKillAndRestore(
            expected = "Arrange your shelves",
            what = "the Library tabs page after the restore",
        )
        returnToTabs()

        visitSettingsSubPages()
    }

    private fun surviveDontKeepActivitiesAcrossTabsBookDetailAndHome() {
        device.executeShellCommand("settings put global always_finish_activities 1")

        visitEveryTab()

        openBookFromLibrary()

        device.pressHome()
        launchApp()

        assertAppAlive("returning from home with Don't keep activities on")
        waitFor(
            labelled("More actions"),
            what = "book detail after returning from home",
        )

        device.pressBack()

        assertAppAlive("going back with Don't keep activities on")
    }

    private fun enabledSaveButton(): UiObject2? =
        clickableAncestor(labelled("^Save API key$")())?.takeIf { button -> button.isEnabled }

    private fun clickableAncestor(label: UiObject2?): UiObject2? {
        var node = label

        while (node != null && node.isClickable.not()) {
            node = node.parent
        }

        return node
    }

    private fun visitEveryTab() {
        for (tab in TABS) {
            clickLabel("^$tab$")

            assertAppAlive("opening the $tab tab")
            waitFor(
                labelled("^$tab$"),
                what = "the $tab tab to stay visible",
            )
        }

        clickLabel("^Settings$")

        waitFor(
            labelled("About Softcover"),
            what = "the Settings menu rows",
        )
    }

    private fun openBookFromLibrary() {
        clickLabel("^Library$")

        val book = waitFor(
            finder = { device.findObject(By.res(BOOK_CARD_TAG)) },
            what = "a book card in the Library",
        )

        book.click()

        waitFor(
            finder = labelled("More actions"),
            what = "book detail (its More actions button)",
        )
    }

    private fun openFullScreenCover() {
        val cover = waitFor(
            finder = { device.findObject(By.res(COVER_TAG)) },
            what = "the cover on book detail",
        )

        cover.click()

        waitFor(
            labelled("Close cover viewer"),
            what = "the full-screen cover viewer",
        )
    }

    private fun openCreateListSheet() {
        clickLabel("^Settings$")
        scrollToAndClick("Library tabs")

        waitFor(
            labelled("Arrange your shelves"),
            what = "the Library tabs page",
        )
        scrollToAndClick("Create a new list")

        waitFor(
            finder = labelled("Create list"),
            what = "the create-list sheet",
        )
    }

    private fun returnToTabs() {
        repeat(MAX_BACK_PRESSES) {
            if (pollFor(labelled("^Explore$"))) return

            assertTrue(
                "The app left the foreground while returning to the bottom bar. Screen:\n${hierarchySummary()}",
                device.hasObject(By.pkg(APP_PACKAGE)),
            )

            device.pressBack()
            device.waitForIdle()
        }

        throw AssertionError("Could not get back to the bottom bar. Screen:\n${hierarchySummary()}")
    }

    private fun visitSettingsSubPages() {
        clickLabel("^Settings$")

        for (page in SETTINGS_SUB_PAGES) {
            scrollToAndClick(page.row)

            waitFor(
                labelled(page.marker),
                what = "the ${page.row} page",
            )
            assertAppAlive("opening ${page.row}")

            device.pressBack()

            waitFor(
                labelled("^Settings$"),
                what = "Settings after leaving ${page.row}",
            )
        }
    }

    private fun pollFor(
        finder: Finder,
        timeoutMillis: Long = BACK_SETTLE_MILLIS,
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMillis

        while (System.currentTimeMillis() < deadline) {
            val found = try {
                finder() != null
            } catch (_: StaleObjectException) {
                false
            }

            if (found) return true

            Thread.sleep(POLL_MILLIS)
        }

        return false
    }

    private fun backgroundKillAndRestore(
        expected: String,
        what: String,
    ) {
        device.pressHome()
        device.waitForIdle()

        // `am kill` only reaps a cached background process, so the app must already be off screen.
        device.executeShellCommand("am kill $APP_PACKAGE")

        launchApp()

        assertAppAlive("restoring after the process was killed")
        waitFor(
            labelled(expected),
            what = what,
        )
    }

    private fun passOnboardingIntro() {
        clickLabel(
            "^Begin$",
            timeoutMillis = LAUNCH_TIMEOUT_MILLIS,
        )
        clickLabel("^Continue$")

        waitFor(
            { device.findObject(By.clazz(EDIT_TEXT_CLASS)) },
            what = "the API-key field on the last onboarding page",
        )
    }

    private fun enterToken(token: String) {
        val field = waitFor(
            { device.findObject(By.clazz(EDIT_TEXT_CLASS)) },
            what = "the API-key field",
        )

        // Neither UiObject2.text nor UiDevice.executeShellCommand: both log the value to logcat, which lands in
        // test output. UiAutomation runs the command without logging it, and without a shell, so no quoting.
        val argument = token
            .replace(
                " ",
                "%s",
            )

        repeat(MAX_TYPE_ATTEMPTS) {
            field.click()

            val focused = pollFor(
                { device.findObject(By.clazz(EDIT_TEXT_CLASS).focused(true)) },
                FOCUS_SETTLE_MILLIS,
            )

            if (focused.not()) return@repeat

            instrumentation.uiAutomation.executeShellCommand("input text $argument").use { descriptor ->
                ParcelFileDescriptor.AutoCloseInputStream(descriptor).readBytes()
            }

            device.waitForIdle()

            val saveEnabled = pollFor(
                { enabledSaveButton() },
                TYPE_SETTLE_MILLIS,
            )

            if (saveEnabled) return

            field.clear()
        }

        throw AssertionError(
            "The token never reached the API-key field (Save API key stayed disabled). Screen:\n${hierarchySummary()}",
        )
    }

    private fun launchApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(APP_PACKAGE)

        assertNotNull(
            "The app under test ($APP_PACKAGE) is not installed or not launchable",
            intent,
        )

        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        assertTrue(
            "The app did not come to the foreground within $LAUNCH_TIMEOUT_MILLIS ms",
            device.wait(
                Until.hasObject(By.pkg(APP_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT_MILLIS,
            ),
        )
    }

    private fun assertAppAlive(step: String) {
        device.waitForIdle()

        val crashDialog = device.hasObject(By.text(Pattern.compile(".*(keeps stopping|isn't responding).*")))

        assertTrue(
            "The app crashed or stopped responding while $step",
            crashDialog.not(),
        )
        assertTrue(
            "The app is no longer in the foreground after $step",
            device.hasObject(By.pkg(APP_PACKAGE)),
        )
    }

    /** Compose exposes labels as text on some nodes and as content descriptions on others; match both. */
    private fun labelled(regex: String): Finder = {
        // An alternative anchored with ^ must match the whole label; any other matches as a substring.
        val body = regex.split("|").joinToString(separator = "|") { alt ->
            if (alt.startsWith("^")) alt else ".*($alt).*"
        }
        val pattern = Pattern.compile(
            body,
            Pattern.CASE_INSENSITIVE or Pattern.DOTALL,
        )

        device.findObject(By.text(pattern)) ?: device.findObject(By.desc(pattern))
    }

    private fun waitFor(
        finder: Finder,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
        what: String,
    ): UiObject2 {
        val deadline = System.currentTimeMillis() + timeoutMillis

        while (System.currentTimeMillis() < deadline) {
            dismissPermissionPrompt()

            val found = try {
                finder()
            } catch (_: StaleObjectException) {
                // The tree changed between find and read; the next poll sees the settled one.
                null
            }

            found?.let { return it }

            Thread.sleep(POLL_MILLIS)
        }

        throw AssertionError("Timed out after $timeoutMillis ms waiting for $what. Screen:\n${hierarchySummary()}")
    }

    private fun clickLabel(
        regex: String,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    ) {
        waitFor(
            labelled(regex),
            timeoutMillis,
            what = "'$regex' to click",
        ).click()

        device.waitForIdle()
    }

    private fun scrollToAndClick(label: String) {
        val finder = labelled(label)

        repeat(MAX_SCROLLS) { attempt ->
            val target = finder()

            // The floating bottom bar overlays the lowest rows, so scroll them clear of it first; on the
            // last attempt the content has run out and the row is tapped where it is.
            val clearOfBottomBar = target != null &&
                target.visibleBounds.centerY() < device.displayHeight * CLEAR_OF_BOTTOM_BAR_SHARE

            if (target != null && (clearOfBottomBar || attempt == MAX_SCROLLS - 1)) {
                target.click()
                device.waitForIdle()

                return
            }

            val width = device.displayWidth
            val height = device.displayHeight

            device.swipe(
                width / 2,
                height * 3 / 4,
                width / 2,
                height / 3,
                SWIPE_STEPS,
            )
            device.waitForIdle()
            Thread.sleep(SETTLE_MILLIS)
        }

        throw AssertionError("Could not scroll to '$label'. Screen:\n${hierarchySummary()}")
    }

    private fun dismissPermissionPrompt() {
        device.findObject(By.res(PERMISSION_DENY_BUTTON))?.click()
    }

    /**
     * The labelled nodes on screen, one per line. Text fields are skipped so the token never reaches a
     * failure message.
     */
    private fun hierarchySummary(): String {
        val stream = ByteArrayOutputStream()

        device.dumpWindowHierarchy(stream)

        return NODE_PATTERN.findAll(stream.toString(Charsets.UTF_8))
            .map { match -> match.value }
            .filterNot { node -> node.contains(EDIT_TEXT_CLASS) }
            .mapNotNull { node ->
                val text = ATTRIBUTE_TEXT.find(node)?.groupValues?.get(1).orEmpty()
                val description = ATTRIBUTE_DESCRIPTION.find(node)?.groupValues?.get(1).orEmpty()
                val clickable = node.contains("clickable=\"true\"")
                val bounds = ATTRIBUTE_BOUNDS.find(node)?.groupValues?.get(1).orEmpty()

                if (text.isEmpty() && description.isEmpty() && clickable.not()) {
                    null
                } else {
                    "text='$text' desc='$description' clickable=$clickable bounds=$bounds"
                }
            }
            .joinToString(separator = "\n")
            .takeLast(HIERARCHY_DUMP_LIMIT)
    }

    private data class SettingsSubPage(
        val row: String,
        val marker: String,
    )

    private companion object {
        val NODE_PATTERN = Regex("<node [^>]*>")
        val ATTRIBUTE_TEXT = Regex(" text=\"([^\"]*)\"")
        val ATTRIBUTE_DESCRIPTION = Regex(" content-desc=\"([^\"]*)\"")
        val ATTRIBUTE_BOUNDS = Regex(" bounds=\"([^\"]*)\"")

        const val APP_PACKAGE = "nl.rhaydus.softcover"
        const val TOKEN_ARGUMENT = "hardcoverToken"
        const val EDIT_TEXT_CLASS = "android.widget.EditText"
        const val PERMISSION_DENY_BUTTON = "com.android.permissioncontroller:id/permission_deny_button"

        const val DEFAULT_TIMEOUT_MILLIS = 15_000L
        const val LAUNCH_TIMEOUT_MILLIS = 30_000L
        const val SYNC_TIMEOUT_MILLIS = 180_000L
        const val REJECTED_KEY_PATTERN = "wasn.t accepted. Double-check"
        const val SETUP_FAILED_PATTERN = "Something went wrong setting up your account"
        const val REJECTED_TOKEN_MESSAGE = "Hardcover rejected the token (HARDCOVER_SMOKE_TOKEN) — rotate/update it"
        const val SETUP_FAILED_MESSAGE = "Sign-in setup failed (network or server), not a token rejection"
        const val POLL_MILLIS = 250L
        const val SETTLE_MILLIS = 600L
        const val CLEAR_OF_BOTTOM_BAR_SHARE = 0.8
        const val MAX_SCROLLS = 6
        const val MAX_BACK_PRESSES = 5
        const val MAX_TYPE_ATTEMPTS = 3
        const val FOCUS_SETTLE_MILLIS = 3_000L
        const val TYPE_SETTLE_MILLIS = 5_000L
        const val BACK_SETTLE_MILLIS = 1_500L
        const val SWIPE_STEPS = 20
        const val HIERARCHY_DUMP_LIMIT = 6_000

        const val BOOK_CARD_TAG = "library_book_card"
        const val COVER_TAG = "book_detail_cover"

        val TABS = listOf("Reading", "Library", "Explore")

        val SETTINGS_SUB_PAGES = listOf(
            SettingsSubPage(
                row = "Your profile",
                marker = "By the numbers|Share your profile",
            ),
            SettingsSubPage(
                row = "Appearance",
                marker = "How Softcover looks",
            ),
            SettingsSubPage(
                row = "Library tabs",
                marker = "Arrange your shelves",
            ),
            SettingsSubPage(
                row = "Widgets",
                marker = "On your home screen|Add a widget",
            ),
            SettingsSubPage(
                row = "Hidden suggestions",
                marker = "Nothing hidden|Hidden suggestions",
            ),
            SettingsSubPage(
                row = "About Softcover",
                marker = "independent, open-source",
            ),
            SettingsSubPage(
                row = "What.s new",
                marker = "What changed in each version",
            ),
            SettingsSubPage(
                row = "Roadmap",
                marker = "What we.re building next",
            ),
        )
    }
}
