plugins {
    id("com.android.library")
    kotlin("android")
}

loadProperties("googleapi.properties", extra)

android {
    compileSdkVersion(V.compileSdkVersion)
    defaultConfig {
        minSdkVersion(V.minSdkVersion)
        targetSdkVersion(V.targetSdkVersion)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val authority = "com.xinthink.muzei.photos"
        manifestPlaceholders = mapOf("photosAuthority" to authority)
        buildConfigField("String", "PHOTOS_AUTHORITY", "\"$authority\"")
        buildConfigField("String", "AUTH_CLIENT_ID", "\"${extra["auth_client_id"]}\"")
        buildConfigField("String", "AUTH_CLIENT_SECRET", "\"${extra["auth_client_secret"]}\"")
        buildConfigField("String", "AUTH_SCOPE_PHOTOS", "\"${extra["auth_scope_photos"]}\"")
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
    api(kt)
    api(coroutinesAndroid)
    api(appCompat)
    api(coreKtx)
    api(preference)
    api(ankoCommons)

    // Firebase
    api(firebaseBoM)
    api(analytics)
    api(crashlytics)

    implementation(muzeiApi)
    implementation(workRuntime)
    implementation(retrofit)
    implementation(retrofitMoshi)
    implementation(okhttpLogging)

    // test
    testImplementation(junit)
    androidTestImplementation(testRunner)
    androidTestImplementation(espressoCore)
}
