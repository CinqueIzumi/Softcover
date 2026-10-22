package nl.rhaydus.softcover.showcase.render

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import nl.rhaydus.softcover.core.component.gallery.GalleryFixture
import nl.rhaydus.softcover.core.component.gallery.GalleryRegistry
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Generator, not a test: draws every [GalleryRegistry] fixture in the default Softcover palette to
 * `<renders>/<brightness>/<family>/<entry>/<fixture>.png` for the design preset. It asserts nothing
 * about the pixels; a fixture that cannot render is skipped and listed in `skipped-<brightness>.txt`.
 * It also writes `index.json`, the entry metadata the preset bundle generates its components from.
 * Runs only through the `renderShowcase` task (see `showcase/build.gradle.kts`).
 */
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [35],
    qualifiers = "w412dp-h915dp-xhdpi",
    application = Application::class,
)
class ShowcaseRenderRunner {
    private val outputDir = File(
        requireNotNull(System.getProperty("showcase.render.dir")) {
            "showcase.render.dir is set by the renderShowcase task"
        },
    )

    @Before
    fun registerActivity() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app.packageManager).addActivityIfNotPresent(
            ComponentName(
                app.packageName,
                ComponentActivity::class.java.name,
            ),
        )
    }

    @Test
    fun everyHandWrittenComponentHasAnEntry() {
        val missing = RequiredComponents.missingFrom(GalleryRegistry.entries)

        check(missing.isEmpty()) { "GalleryRegistry has no entry named: ${missing.joinToString()}" }
    }

    @Test
    fun writeIndex() {
        val entrySlugs = uniqueSlugs(GalleryRegistry.entries.map { it.name })
        val entriesJson = JSONArray()

        GalleryRegistry.entries.forEachIndexed { entryIndex, entry ->
            val fixtureSlugs = uniqueSlugs(entry.fixtures.map { it.label })

            entriesJson.put(
                JSONObject()
                    .put(
                        "name",
                        entry.name,
                    )
                    .put(
                        "family",
                        entry.family.label,
                    )
                    .put(
                        "familySlug",
                        entry.family.name.lowercase(),
                    )
                    .put(
                        "slug",
                        entrySlugs[entryIndex],
                    )
                    .put(
                        "blurb",
                        entry.blurb,
                    )
                    .put(
                        "handWritten",
                        entry.name in RequiredComponents.names,
                    )
                    .put(
                        "fixtures",
                        JSONArray(
                            entry.fixtures.mapIndexed { index, fixture ->
                                JSONObject()
                                    .put(
                                        "label",
                                        fixture.label,
                                    )
                                    .put(
                                        "slug",
                                        fixtureSlugs[index],
                                    )
                            },
                        ),
                    ),
            )
        }

        File(
            outputDir,
            "index.json",
        ).apply { parentFile.mkdirs() }.writeText(entriesJson.toString(2))
    }

    @Test
    fun renderLight() = render(SchemeBrightness.LIGHT)

    @Test
    fun renderDark() = render(SchemeBrightness.DARK)

    @Test
    fun renderDim() = render(SchemeBrightness.DIM)

    private fun render(brightness: SchemeBrightness) {
        val brightnessDir = File(
            outputDir,
            brightness.name.lowercase(),
        )
        val skipped = mutableListOf<String>()

        val entrySlugs = uniqueSlugs(GalleryRegistry.entries.map { it.name })

        GalleryRegistry.entries.forEachIndexed { entryIndex, entry ->
            val entryDir = File(
                brightnessDir,
                "${entry.family.name.lowercase()}/${entrySlugs[entryIndex]}",
            )
            val slugs = uniqueSlugs(entry.fixtures.map { it.label })

            entry.fixtures.forEachIndexed { index, fixture ->
                val file = File(
                    entryDir,
                    "${slugs[index]}.png",
                )

                capture(
                    fixture = fixture,
                    brightness = brightness,
                    file = file,
                ).onFailure { error ->
                    skipped += "${entry.name} / ${fixture.label}: ${error.javaClass.simpleName}: ${error.message}"
                }
            }
        }

        File(
            brightnessDir,
            "skipped-${brightness.name.lowercase()}.txt",
        ).apply { parentFile.mkdirs() }.writeText(skipped.joinToString("\n"))
    }

    @OptIn(ExperimentalTestApi::class)
    private fun capture(
        fixture: GalleryFixture,
        brightness: SchemeBrightness,
        file: File,
    ): Result<Unit> = runCatching {
        file.parentFile.mkdirs()

        runComposeUiTest {
            setContent {
                SoftcoverTheme(
                    brightness = brightness,
                    dynamicColor = false,
                ) {
                    Surface(
                        modifier = Modifier.testTag(CAPTURE_TAG),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Box(modifier = Modifier.padding(Spacing.md)) {
                            fixture.render(Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            onNodeWithTag(CAPTURE_TAG).captureRoboImage(file.path)
        }
    }

    private companion object {
        const val CAPTURE_TAG = "capture"
    }
}
