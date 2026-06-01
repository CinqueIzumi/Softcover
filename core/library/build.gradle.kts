plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.library"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:lists"))
    implementation(project(":core:preferences"))
    implementation(project(":core:identity"))
}
