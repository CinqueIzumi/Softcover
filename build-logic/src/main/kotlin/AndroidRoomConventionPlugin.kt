import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Room runtime + KSP compiler for the single `@Database` owner (`:core:database`).
 *
 * `:core:database` is a Kotlin Multiplatform module (it applies `softcover.kmp.library`), so the Room
 * artifacts attach to source-set-scoped configurations rather than the flat `api`/`ksp` ones a
 * single-variant Android library would expose:
 *
 * - `room-runtime` (the multiplatform artifact — it absorbed the former `room-ktx` coroutine/Flow API
 *   in Room 2.7, so that artifact is no longer needed) goes on `commonMainApi`: `:core:database`
 *   exposes `SoftcoverDatabase` (a `RoomDatabase`) and the raw-query types in its public surface, so
 *   consumers resolve Room transitively across every target.
 * - `room-compiler` runs through KSP **per target** — KMP KSP has no single `ksp` configuration, so
 *   codegen is wired once per compilation (`kspAndroid` + one per iOS target declared by
 *   [KmpLibraryConventionPlugin]) so every target gets its generated `@Database` implementation.
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        dependencies {
            add(
                "commonMainApi",
                libs.library("room-runtime"),
            )
            // Bundled SQLite driver — KMP Room ships no platform driver of its own, so every target
            // opens the database through `BundledSQLiteDriver` (wired in `SoftcoverDatabase.build`).
            add(
                "commonMainImplementation",
                libs.library("androidx-sqlite-bundled"),
            )

            // One ksp<Target> configuration per target compiled — matches the targets declared in
            // KmpLibraryConventionPlugin (the Android target + the three iOS targets).
            listOf(
                "kspAndroid",
                "kspIosArm64",
                "kspIosSimulatorArm64",
                "kspIosX64",
            ).forEach { configuration ->
                add(
                    configuration,
                    libs.library("room-compiler"),
                )
            }
        }
    }
}
