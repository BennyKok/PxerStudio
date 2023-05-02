plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ktlint)
}

android {
    compileSdk = 33
    namespace = "com.benny.pxerstudio"

    defaultConfig {
        applicationId = "com.benny.pxerstudio"
        minSdk = 21
        targetSdk = 33
        versionCode = 9
        versionName = "1.2.0"

        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            resValue("string", "app_name", "PxerStudio")
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "PxerStudio Debug")
        }
    }

    compileOptions {
        encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    packaging {
        resources.excludes.add("/META-INF/*")
    }
}

dependencies {
    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.cardview)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.ktx)
    implementation(libs.material)

    // Third-party
    implementation(libs.material.dialogs.core)
    implementation(libs.material.dialogs.files)
    implementation(libs.material.dialogs.input)
    implementation(libs.fastadapter)
    implementation(libs.fastadapter.extensions.drag)
    implementation(libs.gifencoder.integration)
    implementation(libs.licensesdialog)
    implementation(libs.fab)
    implementation(libs.gson)
}
