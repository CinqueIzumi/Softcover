import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Room runtime + KSP compiler. Applied only by `:core:database` (the single `@Database` owner). */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        dependencies {
            add("implementation", libs.library("room-runtime"))
            add("implementation", libs.library("room-ktx"))
            add("ksp", libs.library("room-compiler"))
        }
    }
}
