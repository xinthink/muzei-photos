import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(V.compileSdkVersion)
    defaultConfig {
        applicationId = "com.xinthink.muzei.photos"
        minSdkVersion(V.minSdkVersion)
        targetSdkVersion(V.targetSdkVersion)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        this as KotlinJvmOptions
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":muzeiPhotos"))
    implementation(kt)
    implementation(coroutinesAndroid)
    implementation(anko)
    implementation(ankoListeners)
    implementation(coreKtx)
    implementation(lifecycleX)
    implementation(viewModelKtx)
    implementation(appCompat)
    implementation(preference)
    implementation(navigationFragment)
    implementation(material)
    implementation(constraintLayout)
    implementation(ankoRecyclerView)

    // google sign-in & photos authorization
    implementation(gmsAuth)

    // test
    testImplementation(junit)
    androidTestImplementation(testRunner)
    androidTestImplementation(espressoCore)
}
