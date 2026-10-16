package nl.rhaydus.softcover.smoketest

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import dalvik.system.PathClassLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier

/**
 * Proves the classes libraries reach by name survive R8 in the installed minified APK. The app's code is
 * loaded through its own class loader and probed by original fully-qualified name, so a renamed or
 * removed class fails here exactly as it would on a user's device.
 */
class MinifiedContractTest {
    private lateinit var appClassLoader: ClassLoader

    @Before
    fun loadAppClassLoader() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val appInfo = context.packageManager.getApplicationInfo(
            APP_PACKAGE,
            0,
        )

        // Deliberately not `createPackageContext(.., CONTEXT_INCLUDE_CODE)`: that registers this process as
        // depending on the app, so a later `pm clear` of the app (the journey does this) kills the test run.
        val apkPaths = listOf(appInfo.sourceDir) + appInfo.splitSourceDirs.orEmpty()

        appClassLoader = PathClassLoader(
            apkPaths.joinToString(separator = File.pathSeparator),
            appInfo.nativeLibraryDir,
            Context::class.java.classLoader,
        )
    }

    @Test
    fun glanceActionCallbacksAreInstantiableByOriginalName() {
        val failures = GLANCE_CALLBACKS.mapNotNull { (name, consequence) ->
            failureOrNull(name, consequence) {
                val callback = load(name).getDeclaredConstructor().newInstance()

                assertTrue(callback.javaClass.interfaces.isNotEmpty())
            }
        }

        failOnAny(failures)
    }

    @Test
    fun glanceAppWidgetsLoadUnderTheirOriginalNamesAndStayDistinct() {
        val failures = GLANCE_WIDGETS.mapNotNull { (name, consequence) ->
            failureOrNull(
                name = name,
                consequence = consequence,
            )
        }

        failOnAny(failures)

        val distinct = GLANCE_WIDGETS.map { it.first }.map(::load).toSet()

        assertEquals(
            "R8 merged two Glance widgets into one class",
            GLANCE_WIDGETS.size,
            distinct.size,
        )
    }

    @Test
    fun coroutineWorkersExposeTheConstructorWorkManagerCalls() {
        val failures = WORKERS.mapNotNull { (name, consequence) ->
            failureOrNull(name, consequence) {
                val constructor = load(name).getConstructor(
                    Context::class.java,
                    load(WORKER_PARAMETERS_CLASS_NAME),
                )

                assertTrue(Modifier.isPublic(constructor.modifiers))
            }
        }

        failOnAny(failures)
    }

    @Test
    fun mlKitComponentRegistrarsKeepTheConstructorFirebaseDiscoveryCalls() {
        val failures = ML_KIT_REGISTRARS.mapNotNull { name ->
            failureOrNull(name, "the barcode scanner cannot initialise") {
                load(name).getDeclaredConstructor()
            }
        }

        failOnAny(failures)
    }

    @Test
    fun roomDatabaseImplementationLoads() {
        val failures = listOfNotNull(
            failureOrNull(
                name = DATABASE_IMPL,
                consequence = "opening the database throws and the app cannot start",
            ),
        )

        failOnAny(failures)
    }

    @Test
    fun voyagerScreensAndTabsLoadUnderTheirOriginalNames() {
        val failures = VOYAGER_DESTINATIONS.mapNotNull { name ->
            failureOrNull(
                name = name,
                consequence = "navigating to this screen fails after process restore, which re-creates it by name",
            )
        }

        failOnAny(failures)
    }

    private fun load(name: String): Class<*> = Class.forName(
        name,
        false,
        appClassLoader,
    )

    private fun failureOrNull(
        name: String,
        consequence: String,
    ): String? = failureOrNull(
        name = name,
        consequence = consequence,
        block = { load(name).name },
    )

    private fun failureOrNull(
        name: String,
        consequence: String,
        block: () -> Any,
    ): String? = try {
        block()
        null
    } catch (error: ReflectiveOperationException) {
        describe(
            name,
            error,
            consequence,
        )
    } catch (error: LinkageError) {
        describe(
            name,
            error,
            consequence,
        )
    } catch (error: AssertionError) {
        describe(
            name,
            error,
            consequence,
        )
    }

    private fun describe(
        name: String,
        error: Throwable,
        consequence: String,
    ): String = "$name is missing or unusable after R8 ($error): $consequence"

    private fun failOnAny(failures: List<String>) {
        assertTrue(
            failures.joinToString(separator = "\n"),
            failures.isEmpty(),
        )
    }

    private companion object {
        const val APP_PACKAGE = "nl.rhaydus.softcover"
        const val WORKER_PARAMETERS_CLASS_NAME = "androidx.work.WorkerParameters"
        const val WIDGET_PACKAGE = "nl.rhaydus.softcover.feature.widget.presentation"
        const val DATABASE_IMPL = "nl.rhaydus.softcover.core.database.SoftcoverDatabase_Impl"

        val GLANCE_CALLBACKS = listOf(
            "$WIDGET_PACKAGE.callback.CycleQuoteCallback" to "tapping the Quote widget fails",
            "$WIDGET_PACKAGE.callback.CycleBookCallback" to "tapping the Currently Reading widget fails",
            "$WIDGET_PACKAGE.callback.ShuffleCallback" to "tapping the Random Pick widget fails",
            "$WIDGET_PACKAGE.callback.TrendingPageCallback" to "paging the Trending widget fails",
        )

        val GLANCE_WIDGETS = listOf(
            "$WIDGET_PACKAGE.glance.QuoteWidget" to "the Quote widget cannot be added or refreshed",
            "$WIDGET_PACKAGE.glance.CurrentlyReadingWidget" to "the Currently Reading widget cannot be added or refreshed",
            "$WIDGET_PACKAGE.glance.RandomPickWidget" to "the Random Pick widget cannot be added or refreshed",
            "$WIDGET_PACKAGE.glance.TrendingWidget" to "the Trending widget cannot be added or refreshed",
        )

        val WORKERS = listOf(
            "$WIDGET_PACKAGE.worker.QuoteRefreshWorker" to "the Quote widget stops refreshing in the background",
            "$WIDGET_PACKAGE.worker.TrendingRefreshWorker" to "the Trending widget stops refreshing in the background",
            "$WIDGET_PACKAGE.worker.TrendingRescheduleWorker" to "the Trending widget stops being rescheduled",
            "$WIDGET_PACKAGE.worker.WidgetMaintenanceWorker" to "widget maintenance never runs",
            "$WIDGET_PACKAGE.worker.WidgetMaintenanceCancelWorker" to "widget maintenance is never cancelled",
        )

        val ML_KIT_REGISTRARS = listOf(
            "com.google.mlkit.common.internal.CommonComponentRegistrar",
            "com.google.mlkit.vision.barcode.internal.BarcodeRegistrar",
            "com.google.mlkit.vision.common.internal.VisionCommonRegistrar",
        )

        val VOYAGER_DESTINATIONS = listOf(
            "nl.rhaydus.softcover.orchestration.presentation.RootScreen",
            "nl.rhaydus.softcover.orchestration.presentation.BottomBarScreen",
            "nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab",
            "nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingScreen",
            "nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab",
            "nl.rhaydus.softcover.feature.library.presentation.screen.LibraryScreen",
            "nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab",
            "nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreScreen",
            "nl.rhaydus.softcover.feature.explore.presentation.screen.HiddenSuggestionsScreen",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsScreen",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.AboutScreen",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.AppearanceSettingsScreen",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.LibraryVisibilitySettingsScreen",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.WidgetsSettingsScreen",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.DocumentPageScreen",
            "nl.rhaydus.softcover.feature.settings.presentation.screen.ComponentGalleryScreen",
            "nl.rhaydus.softcover.feature.profile.presentation.screen.ProfileScreen",
            "nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen",
            "nl.rhaydus.softcover.feature.book_detail.presentation.screen.FullScreenCoverScreen",
            "nl.rhaydus.softcover.feature.onboarding.presentation.screen.OnboardingScreen",
            "nl.rhaydus.softcover.feature.session.presentation.screen.FocusModeScreen",
            "nl.rhaydus.softcover.feature.lists.presentation.screen.CreateListScreen",
            "nl.rhaydus.softcover.feature.scan.presentation.screen.BarcodeScannerScreen",
            "$WIDGET_PACKAGE.screen.RandomPickSettingsScreen",
            "$WIDGET_PACKAGE.screen.TrendingSettingsScreen",
            "$WIDGET_PACKAGE.screen.UpdateProgressScreen",
        )
    }
}
