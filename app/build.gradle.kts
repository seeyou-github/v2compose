plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

// 1. 定义判断逻辑：是否包含 Google 相关的构建任务
val isGoogleTask = gradle.startParameter.taskRequests.any { request ->
    request.args.any { it.contains("Google", ignoreCase = true) }
}

// 2. 只有在执行 Google 相关任务时才动态应用插件
if (isGoogleTask) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.crashlytics.get().pluginId)
}

android {
    namespace = "io.github.v2compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.v2compose"
        minSdk = 26
        targetSdk = 35
        versionCode = 101
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    flavorDimensions += "tracking"
    productFlavors {
        create("foss") {
            dimension = "tracking"
            multiDexEnabled = true
        }
        create("google") {
            dimension = "tracking"
            multiDexEnabled = true
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":htmlText"))

    // Jetpack
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.navigation.compose)

    // Dependency Injection
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.paging.compose)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.startup.runtime)

    implementation(libs.androidx.work.runtime.ktx)

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)

    // firebase (flavor specific)
    "googleImplementation"(platform(libs.firebase.bom))
    "googleImplementation"(libs.firebase.crashlytics.ktx)
    "googleImplementation"(libs.firebase.analytics.ktx)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // v2er-ghui
    implementation(libs.fruit)
    implementation(libs.jsoup) // 保持对 Jsoup 的依赖以兼容旧代码
    implementation("com.fleeksoft.ksoup:ksoup:0.2.6") 
    ksp(libs.fruit.ksp)
    implementation(libs.fruit.converter.retrofit)

    // network
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.google.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.orhanobut.logger)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // coil
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.compose)

    // ui composables
    implementation(libs.ssjetpack.progress.button)
    implementation(libs.toolbar.compose)
    implementation(libs.eventbus)

    // markdown
    implementation(libs.bundles.markwon)

    implementation(libs.compose.webview)
}
