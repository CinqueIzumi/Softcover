import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Room runtime + KSP compiler. Applied only by `:core:database` (the single `@Database` owner). */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        dependencies {
            // api: `:core:database` exposes SoftcoverDatabase (a RoomDatabase) and SupportSQLite query
            // types in its public surface, so consumers need Room + androidx.sqlite transitively.
            add(
                "api",
                libs.library("room-runtime"),
            )
            add(
                "api",
                libs.library("room-ktx"),
            )
            add(
                "ksp",
                libs.library("room-compiler"),
            )
        }
    }
}
