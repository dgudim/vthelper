import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

var appId = "noorg.kloud.vthelper"
var appVersion = "1.0.0"
var appVersionCode = 1

// NOTE: Don't forget to also add the plugins to build.gradle.kts in the root module
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)

    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    // Serialization
    alias(libs.plugins.jetbrains.kotlin.serialization)
    // Kotlin annotation processing
    alias(libs.plugins.ksp)
    // https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
    // room compiler configuration
    alias(libs.plugins.androidx.room)
    // https://proandroiddev.com/compose-stability-analyzer-real-time-stability-insights-for-jetpack-compose-1399924a0a64
    alias(libs.plugins.stabilityAnalyzer)
    // https://github.com/Kotlin/kotlinx-atomicfu#apply-plugin
    alias(libs.plugins.atomicFu)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.startupRuntime)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            // UI Base
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)

            // UI and theming
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)

            // 3d-party ui
            implementation(libs.kizitonwose.calendar)
            implementation(libs.vico.compose)
            implementation(libs.vico.compose.m3)

            // Debugging and developing
            implementation(libs.compose.uiToolingPreview)

            // Functional base
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)

            // Storage
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            // Network
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)
            implementation(libs.kotlinx.serialization.json)
        }
        nativeMain.dependencies { // Only ios
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

// Provided by androidApplication plugin
android {
    namespace = appId
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = appId
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
}

compose.desktop {
    application {
        mainClass = "$appId.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = appId
            packageVersion = appVersion
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// https://github.com/Kotlin/kotlinx-atomicfu#apply-plugin
atomicfu {
    dependenciesVersion = libs.versions.atomicfu.get()
    jvmVariant = "VH"
}
