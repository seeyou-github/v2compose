plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

// 对应 Groovy 的 getGradle().getStartParameter()...
val isGoogleFlavorRequested = gradle.startParameter.taskRequests.toString().contains("Google")
if (isGoogleFlavorRequested) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "io.github.v2compose"
    compileSdk = 35

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
    kotlinOptions {
        jvmTarget = "17"
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
        }
        create("google") {
            dimension = "tracking"
        }
    }
}

dependencies {
    val navVersion = "2.8.7"
    val lifecycleVersion = "2.6.1"
    val hiltVersion = "2.55"
    val hiltAndroidXVersion = "1.2.0"
    val pagingVersion = "3.3.6"
    val coilVersion = "2.7.0"

    implementation(project(":htmlText"))

    // Jetpack
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.navigation:navigation-compose:$navVersion")

    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltAndroidXVersion")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingVersion")

    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.startup:startup-runtime:1.2.0")

    val workVersion = "2.10.0"
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("androidx.hilt:hilt-work:$hiltAndroidXVersion")
    ksp("androidx.hilt:hilt-compiler:$hiltAndroidXVersion")

    // compose
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")

    // firebase (flavor specific)
    "googleImplementation"(platform("com.google.firebase:firebase-bom:33.9.0"))
    "googleImplementation"("com.google.firebase:firebase-crashlytics-ktx")
    "googleImplementation"("com.google.firebase:firebase-analytics-ktx")

    // test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // v2er-ghui
    implementation("me.ghui:Fruit:1.0.4")
    implementation("me.ghui:fruit-converter-retrofit:1.0.5")
    implementation("me.ghui:global-retrofit-converter:1.0.2")

    // network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.orhanobut:logger:2.2.0")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    // coil
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")
    implementation("io.coil-kt:coil-svg:2.6.0")
    implementation("io.coil-kt:coil-compose:$coilVersion")

    // ui composables
    implementation("com.github.SimformSolutionsPvtLtd:SSJetPackComposeProgressButton:1.1.0")
    implementation("me.onebone:toolbar-compose:2.3.5")
    implementation("org.greenrobot:eventbus:3.3.1")

    // markdown
    val markwonVersion = "4.6.2"
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:ext-strikethrough:$markwonVersion")
    implementation("io.noties.markwon:ext-tables:$markwonVersion")
    implementation("io.noties.markwon:html:$markwonVersion")
    implementation("io.noties.markwon:image-coil:$markwonVersion")
    implementation("io.noties.markwon:inline-parser:$markwonVersion")

    implementation("io.github.kevinnzou:compose-webview:0.33.6")
}
