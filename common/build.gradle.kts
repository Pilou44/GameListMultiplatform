import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0-alpha3"
    id("com.android.library")
}

group = "com.wechantloup.gameListManager"
version = "1.0"

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)

                implementation("androidx.compose.material:material:1.1.0-beta04")
                implementation("com.google.android.material:compose-theme-adapter:1.1.2")
                implementation("androidx.compose.ui:ui:1.0.5")
                implementation("androidx.compose.animation:animation-core:1.0.5")
                implementation("androidx.activity:activity-compose:1.4.0")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC3")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
                implementation("io.github.microutils:kotlin-logging:2.1.20")
                implementation("xmlpull:xmlpull:1.1.3.1")
                implementation("org.json:json:20211205")
                implementation("com.hierynomus:smbj:0.11.3")
                implementation("com.google.code.gson:gson:2.8.9")
            }
        }
        named("androidMain") {
            dependencies {
                api("androidx.appcompat:appcompat:1.4.0")
                api("androidx.core:core-ktx:1.7.0")
            }
        }
        named("desktopMain") {
            dependencies {
                api(compose.preview)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
