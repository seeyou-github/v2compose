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
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "VERSION_NAME", "\"1.0.1\"")
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
            implementation(compose.runtime)
            implementation(libs.kotlinx.serialization.json)

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
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.slf4j.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.fruit.ksp)
}

// 解决 KMP 下手动引入 KSP 源码导致的 Task 依赖校验问题
tasks.configureEach {
    if (name.startsWith("compileKotlin") || name.startsWith("ksp")) {
        if (name != "kspCommonMainKotlinMetadata") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}

