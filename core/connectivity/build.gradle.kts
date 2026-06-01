plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.connectivity"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:book"))
    implementation(project(":core:lists"))
}
