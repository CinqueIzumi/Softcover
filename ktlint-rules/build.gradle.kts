plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.ktlint.ruleEngine)
    implementation(libs.ktlint.ruleEngineCore)
    implementation(libs.ktlint.cliRulesetCore)

    // ktlint's rule-engine logs via slf4j; provide the api + a no-op binding at runtime
    implementation("org.slf4j:slf4j-api:2.0.16")
    runtimeOnly("org.slf4j:slf4j-nop:2.0.16")
}

kotlin {
    jvmToolchain(17)
}

// Repo-wide tasks. `ktlintFormat` autofixes; `ktlintCheck` gates (the root build wires every
// module's `check` to depend on it). Both drive the custom ruleset via ktlint's rule-engine.
// `-Pktlint.root=<dir>` scopes the scan (used for testing the rule on a throwaway dir); defaults to
// the whole repo.
val ktlintScanRoot = (project.findProperty("ktlint.root") as String?) ?: rootDir.absolutePath

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Auto-wraps multi-arg calls/declarations across the repo (custom ktlint ruleset)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("nl.rhaydus.ktlint.MainKt")
    args("format", ktlintScanRoot)
}

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Fails the build on custom-ruleset violations (multi-arg one-per-line wrapping)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("nl.rhaydus.ktlint.MainKt")
    args("check", ktlintScanRoot)
}
