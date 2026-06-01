plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.lists"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:book"))
}
