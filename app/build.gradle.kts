/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

import org.lineageos.generatebp.GenerateBp

plugins {
    id("com.android.application")
    id("kotlin-android")
}

buildscript {
    dependencies {
        classpath(":generateBpPlugin")
    }
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "org.lineageos.glimpse"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            // Enables code shrinking, obfuscation, and optimization.
            isMinifyEnabled = true

            // Enables resource shrinking.
            isShrinkResources = true

            // Includes the default ProGuard rules files.
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
        getByName("debug") {
            // Append .dev to package name so we won't conflict with AOSP build.
            applicationIdSuffix = ".dev"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.android.material:material:1.9.0")

    // EXIF
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // Media3
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.1.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.1.1")
    implementation("androidx.media3:media3-exoplayer-rtsp:1.1.1")
    implementation("androidx.media3:media3-exoplayer-smoothstreaming:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Coil
    implementation("io.coil-kt:coil:2.2.2")
    implementation("io.coil-kt:coil-gif:2.2.2")
    implementation("io.coil-kt:coil-video:2.2.2")
}

tasks.register("generateBp") {
    GenerateBp(project, android) { group: String, artifactId: String ->
        when {
            group.startsWith("androidx") -> {
                // We provide our own androidx.media3 & lifecycle-common
                !group.startsWith("androidx.media3") && artifactId != "lifecycle-common"
            }
            group.startsWith("org.jetbrains") -> true
            group == "com.google.auto.value" -> true
            group == "com.google.guava" -> true
            group == "junit" -> true
            else -> false
        }
    }()
}
