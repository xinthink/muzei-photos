plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("io.fabric")
}

android {
    compileSdkVersion(V.compileSdkVersion)
    defaultConfig {
        applicationId = "com.xinthink.muzei.photos"
        minSdkVersion(V.minSdkVersion)
        targetSdkVersion(V.targetSdkVersion)
        versionCode = 7
        versionName = "1.5.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        val keyFile = rootProject.findProperty("keystore.props.file") as String?
        if (!keyFile.isNullOrEmpty()) {
            loadProperties(keyFile)
            val store = findProperty("store")
            if (store != null && file(store).exists()) {
                // println("keystore for release builds: $store")
                register("release") {
                    storeFile = file(store)
                    keyAlias = findProperty("alias") as String?
                    storePassword = findProperty("storePass") as String?
                    keyPassword = findProperty("pass") as String?
                }
            }
        }
    }
    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
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
    implementation(project(":muzeiPhotos"))
    implementation(anko)
    implementation(ankoListeners)
    implementation(lifecycleX)
    implementation(viewModelKtx)
    implementation(material)
    implementation(constraintLayout)
    implementation(ankoCardView)
    implementation(ankoRecyclerView)
    implementation(ankoConstraintLayout)
    implementation(picasso)
    implementation(picassoTransformations)

    // Firebase
    implementation(crashlytics)

    // google sign-in & photos authorization
    implementation(gmsAuth)

    // test
    testImplementation(junit)
    androidTestImplementation(testRunner)
    androidTestImplementation(espressoCore)
}

apply {
    plugin("com.google.gms.google-services")
}
