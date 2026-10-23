package nl.rhaydus.softcover.core.component.gallery

import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GalleryCoverageTest {
    private val exemptions = mapOf(
        "VerticalDivider" to "drawn by the Divider entry",
        "MastheadSubtitle" to "part of PageMasthead",
        "LazySheetScaffold" to "the lazy form of SheetScaffold",
        "AdaptiveModalSheet" to "the sheet host; entries draw sheet bodies inline",
        "SkeletonRevealGroup" to "draws nothing itself",
        "RevealedWhenPresent" to "animates caller content in; draws nothing itself",
        "ExitingItems" to "animates caller items out; draws nothing itself",
    )

    private fun publicComposableNames(): Set<String> = Konsist
        .scopeFromProduction()
        .files
        .filter { file ->
            val path = file.path.replace(
                '\\',
                '/',
            )
            val inComponentSources = "core/component/src/commonMain/" in path
            val inGalleryPackage = "/component/gallery/" in path
            inComponentSources && inGalleryPackage.not()
        }
        .flatMap {
            it.functions(
                includeNested = false,
                includeLocal = false,
            )
        }
        .filter(::isPublicComposable)
        .map { it.name }
        .toSet()

    private fun isPublicComposable(function: KoFunctionDeclaration): Boolean = function.hasAnnotationWithName("Composable") &&
        function.hasModifier(KoModifier.INTERNAL).not() &&
        function.hasModifier(KoModifier.PRIVATE).not() &&
        function.receiverType == null &&
        function.name.first().isUpperCase()

    @Nested
    inner class EveryComposableHasAnEntry {
        @Test
        fun `every non-exempt public composable names a gallery registry entry`() {
            // ----- Arrange -----
            val registered = GalleryRegistry.entries.map { it.name }.toSet()
            val required = publicComposableNames() - exemptions.keys

            // ----- Act -----
            val missing = (required - registered).sorted()

            // ----- Assert -----
            withClue("Add a GalleryRegistry entry (component-contract 7.5) for: $missing") {
                missing.shouldBeEmpty()
            }
        }
    }

    @Nested
    inner class ExemptionsStayCurrent {
        @Test
        fun `every exemption still names an existing public composable`() {
            // ----- Arrange -----
            val composables = publicComposableNames()

            // ----- Act -----
            val stale = (exemptions.keys - composables).sorted()

            // ----- Assert -----
            withClue("Remove exemptions that no longer name a public composable: $stale") {
                stale.shouldBeEmpty()
            }
        }
    }
}
