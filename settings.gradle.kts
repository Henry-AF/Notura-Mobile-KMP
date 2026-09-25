rootProject.name = "NoturaMobileKMP"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":shared")

// :composeApp needs the Android Gradle Plugin and androidx artifacts, which are only
// published on Google Maven. Environments without access to it can still build and test
// :shared with -Pnotura.includeApp=false.
if (providers.gradleProperty("notura.includeApp").orNull != "false") {
    include(":composeApp")
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
