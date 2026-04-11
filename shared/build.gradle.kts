plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildkonfig)
}

buildkonfig {
    packageName = "io.github.v2compose"
    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "VERSION_NAME",
            "\"1.0.1\""
        )
    }
}


kotlin {
    android {
        namespace = "io.github.v2compose.shared"
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ui.tooling.preview)

            // KMP Lifecycle & Navigation
            api(libs.jetbrains.lifecycle.viewmodel.compose)
            api(libs.jetbrains.lifecycle.runtime.compose)
            api(libs.jetbrains.navigation.compose)

            // KMP Koin Compose
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            // Compose Multiplatform Resources
            api(compose.components.resources)

            // Ktor
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            // Fruit-KT KMP
            api(libs.fruit)
            implementation(libs.ksoup)

            // DataStore KMP
            api(libs.androidx.datastore.preferences)

            // Paging
            api(libs.androidx.paging.common)
            api(libs.androidx.paging.compose)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.gif)
            implementation(libs.coil.svg)
            implementation(libs.mikepenz.markdown)
            implementation(libs.mikepenz.markdown.m3)
            implementation(libs.mikepenz.markdown.coil3)
            implementation(project(":htmlText"))
            api(libs.compose.webview)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.slf4j.android)
            implementation(libs.orhanobut.logger)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.koin.androidx.workmanager)
            implementation(libs.okhttp.logging)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.fruit.ksp)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "v2compose.shared.generated.resources"
    generateResClass = always
}

// 解决 KMP 下手动引入 KSP 源码导致的 Task 依赖校验问题
tasks.configureEach {
    if (name.startsWith("compileKotlin") || name.startsWith("ksp")) {
        if (name != "kspCommonMainKotlinMetadata") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}


// Wire the copied assets Directory to the KMP Android library's compilation
kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    android { // this is AndroidTarget
        // We can't access AGP assets directly in experimental KMP plugin via standard properties yet,
        // so we inject it into the app's merge assets task below if needed.
    }
}

// Fix for missing outputDirectory in copyAndroidMainComposeResourcesToAndroidAssets when using android.kotlin.multiplatform.library
tasks.configureEach {
    if (name == "copyAndroidMainComposeResourcesToAndroidAssets") {
        try {
            val dirProp = this.property("outputDirectory") as org.gradle.api.file.DirectoryProperty
            dirProp.set(layout.buildDirectory.dir("intermediates/compose_fake_assets"))
        } catch (e: Exception) {
        }
    }
}
