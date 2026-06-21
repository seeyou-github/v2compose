plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val appVersionCode = providers.gradleProperty("app.versionCode").get().toInt()
val appVersionName = providers.gradleProperty("app.versionName").get()

android {
    namespace = "io.github.v2compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.v2compose"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("Test.jks")
            storePassword = "123456"
            keyAlias = "Test"
            keyPassword = "123456"
        }
        getByName("debug") {
            storeFile = file("Test.jks")
            storePassword = "123456"
            keyAlias = "Test"
            keyPassword = "123456"
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
}

dependencies {
    implementation(project(":shared"))

    // Jetpack
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.workmanager)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.work.runtime.ktx)

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // image loading
    implementation(libs.coil)
}


// Workaround: Add Compose Resources generated in shared KMP module correctly structured to app assets
android {
    sourceSets.getByName("main") {
        val sharedDir =
            project(":shared").layout.buildDirectory.dir("intermediates/compose_fake_assets")
                .get().asFile.absolutePath
        assets.directories.add(sharedDir)
    }
}

val copySharedComposeResourcesToAndroidAssets = project(":shared").tasks.matching {
    it.name == "copyAndroidMainComposeResourcesToAndroidAssets"
}

tasks.matching {
    (it.name.startsWith("merge") && it.name.endsWith("Assets")) ||
        (it.name.startsWith("generate") && it.name.endsWith("LintVitalReportModel")) ||
        it.name.startsWith("lintVitalAnalyze")
}.configureEach {
    dependsOn(copySharedComposeResourcesToAndroidAssets)
}
