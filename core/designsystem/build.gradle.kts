plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

// Compose Multiplatform resource accessor: keep `Res` internal to this module (consumers reach
// drawables through the `SoftcoverIcon` catalog, never CMP's resource runtime directly).
compose.resources {
    publicResClass = false
    packageOfResClass = "nl.rhaydus.softcover.core.designsystem.generated.resources"
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.designsystem"

        // The KMP Android library plugin keeps Android resources off by default; this module ships
        // the shared drawables/strings/themes, so opt them in.
        androidResources.enable = true
    }

    // Build-time design-token export (`exportDesignTokens`). A JVM-only compilation that sees this module's
    // `internal` palette and type builders; nothing in it reaches the Android or iOS builds, nor the APK.
    val jvmTarget = jvm()
    val designPreset = jvmTarget.compilations.create("designPreset")
    designPreset.associateWith(jvmTarget.compilations.getByName("main"))

    val designPresetDir = layout.buildDirectory.dir("design-preset/project")

    // The classpath is the exporter's own output plus the module it reads: `associateWith` makes the
    // main output visible at compile time only.
    fun registerDesignPresetTask(
        taskName: String,
        taskDescription: String,
        mode: String,
    ): TaskProvider<JavaExec> = tasks.register<JavaExec>(taskName) {
        group = "design preset"
        description = taskDescription
        classpath = files(
            designPreset.output.allOutputs,
            jvmTarget.compilations.getByName("main").output.allOutputs,
            designPreset.runtimeDependencyFiles,
        )
        mainClass.set("nl.rhaydus.softcover.core.designsystem.preset.MainKt")
        args(
            mode,
            "--module-dir=${projectDir.absolutePath}",
            "--out=${designPresetDir.get().asFile.absolutePath}",
            "--design-dir=${rootProject.layout.projectDirectory.dir("design/project").asFile.absolutePath}",
        )
        inputs.files(fileTree("src/commonMain/composeResources"), fileTree("src/androidMain/res"))
    }

    registerDesignPresetTask(
        taskName = "exportDesignTokens",
        taskDescription = "Exports this module's design tokens, fonts and vector assets into build/design-preset.",
        mode = "export",
    ).configure {
        outputs.dir(designPresetDir)
    }

    registerDesignPresetTask(
        taskName = "checkDesignPresetContent",
        taskDescription = "Fails when design/project cites a token that does not exist or hard-codes a colour.",
        mode = "check",
    )

    // Unit tests for the exporter. The test compilation is associated with `designPreset`, which is itself
    // associated with `main`, so both sets of `internal` declarations are visible.
    val designPresetTest = jvmTarget.compilations.create("designPresetTest")
    designPresetTest.associateWith(designPreset)
    designPresetTest.associateWith(jvmTarget.compilations.getByName("main"))

    val testDesignPreset = tasks.register<Test>("testDesignPreset") {
        group = "verification"
        description = "Runs the unit tests of the design-token exporter."
        testClassesDirs = files(designPresetTest.output.classesDirs)
        classpath = files(
            designPresetTest.output.allOutputs,
            designPreset.output.allOutputs,
            jvmTarget.compilations.getByName("main").output.allOutputs,
            designPresetTest.runtimeDependencyFiles,
        )
        useJUnitPlatform()
    }
    tasks.named("check") { dependsOn(testDesignPreset) }

    sourceSets {
        commonMain.dependencies {
            // NO `project(...)` dependency belongs here, and `checkModuleGraph` fails the build if one
            // appears (migration tracker § 6, G2). This module is tokens — theme, editorial typography,
            // the icon/illustration catalogs, modifiers, the bottom-chrome padding seam,
            // shared-element scopes — and a token has nothing
            // to ask of the rest of the app. The source-level half of the same gate is a detekt
            // `ForbiddenImport` scoped to `**/core/designsystem/**` in `config/detekt/detekt.yml`.
            //
            // Three dependencies left when the `Deadline*` trio did, and all three had to go in the
            // same commit that emptied them because `onUnusedDependencies` is `severity("fail")`:
            // `:core:domain` (the trio's domain types), `kotlinx-datetime` and `core-common`
            // (`secondsToHm` for the pace line, `currentLocalDate` for the release-date formatters).
            // `:core:component` and `:core:uibinding` declare the latter two themselves now, rather than
            // inheriting them through the `api` edges that used to sit here.
            api(libs.rhaydus.designsystemCore)

            implementation(libs.rhaydus.designsystemEditorial)
        }

        // The exporter tells a public top-level colour from an internal one, which bytecode does not record.
        getByName("jvmDesignPreset").dependencies {
            implementation(kotlin("reflect"))
        }

        getByName("jvmDesignPresetTest").dependencies {
            implementation(libs.junit.api)
            implementation(libs.kotest)
            runtimeOnly(libs.junit.engine)
            runtimeOnly(libs.junit.platform.launcher)
        }

        androidMain.dependencies {
            // Provides the Material Components `Theme.Material3.*` XML themes the Android manifest/resources
            // reference. Used only from XML (not Kotlin), so dependency-analysis can't see it — excluded from
            // the buildHealth unused-check in the root build.
            implementation(libs.material.components)
        }
    }
}
