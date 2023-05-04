plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.ktlint) apply false
}

val detektFormatting = libs.detekt.formatting

allprojects {
    apply(
        plugin = "io.gitlab.arturbosch.detekt"
    )

    detekt {
        buildUponDefaultConfig = true
        autoCorrect = true
        config = files("$rootDir/gradle/detekt.yml")
    }

    dependencies {
        detektPlugins(detektFormatting)
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}
