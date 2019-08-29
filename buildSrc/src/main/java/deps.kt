import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin

/**
 * Dependency versions
 */
object V {
    const val compileSdkVersion = 28
    const val targetSdkVersion = 28
    const val minSdkVersion = 19
    const val androidPluginVersion = "3.4.1"

    const val kotlinVersion = "1.3.41"
    const val ktStdlib = "stdlib-jdk7"
    const val coroutinesVersion = "1.3.0-RC2"
    const val ankoVersion = "0.10.8"

    const val appCompatVersion = "1.1.0-rc01"
    const val browserVersion = "1.0.0"
    const val constraintLayoutVersion = "1.1.3"
    const val coreVersion = "1.2.0-alpha03"
    const val archVersion = "2.0.0-rc01"
    const val lifecycleVersion = "2.2.0-alpha03"
    const val navVersion = "1.0.0-rc02"
    const val preferenceVersion = "1.1.0-alpha03"
    const val materialVersion = "1.1.0-alpha09"
    const val exifInterfaceVersion = "1.0.0"
    const val retrofitVersion = "2.6.1"
    const val okhttpVersion = "3.12.1"
    const val picassoVersion = "2.71828"
    const val picassoTransformationsVersion = "2.2.1"
    const val workManagerVersion = "1.0.0"
    const val muzeiApiVersion = "3.1.0"
    const val gmsAuthVersion = "16.0.1"

    const val ktlintVersion = "0.34.2"
    const val junitVersion = "4.12"
    const val testRunnerVersion = "1.2.0"
    const val espressoVersion = "3.2.0"
}

// plugin dependencies
val DependencyHandler.androidPlugin get() = "com.android.tools.build:gradle:${V.androidPluginVersion}"
val DependencyHandler.kotlinPlugin get() = "org.jetbrains.kotlin:kotlin-gradle-plugin:${V.kotlinVersion}"
val DependencyHandler.ktlint get() = "com.pinterest:ktlint:${V.ktlintVersion}"


/** Kotlin dependency with default stdlib module */
val DependencyHandler.kt get() = kotlin(V.ktStdlib, V.kotlinVersion)
val DependencyHandler.coroutinesCore get() = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${V.coroutinesVersion}"
val DependencyHandler.coroutinesAndroid get() = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${V.coroutinesVersion}"
val DependencyHandler.ankoCommons get() = "org.jetbrains.anko:anko-commons:${V.ankoVersion}"
val DependencyHandler.anko get() = "org.jetbrains.anko:anko-sdk19:${V.ankoVersion}"
val DependencyHandler.ankoCoroutines get() = "org.jetbrains.anko:anko-coroutines:${V.ankoVersion}"
val DependencyHandler.ankoListeners get() = "org.jetbrains.anko:anko-sdk19-listeners:${V.ankoVersion}"
val DependencyHandler.ankoDesign get() = "org.jetbrains.anko:anko-design:${V.ankoVersion}"
val DependencyHandler.ankoCardView get() = "org.jetbrains.anko:anko-cardview-v7:${V.ankoVersion}"
val DependencyHandler.ankoRecyclerView get() = "org.jetbrains.anko:anko-recyclerview-v7:${V.ankoVersion}"
val DependencyHandler.ankoConstraintLayout get() = "org.jetbrains.anko:anko-constraint-layout:${V.ankoVersion}"

// dependencies
val DependencyHandler.appCompat get() = "androidx.appcompat:appcompat:${V.appCompatVersion}"
val DependencyHandler.coreKtx get() = "androidx.core:core-ktx:${V.coreVersion}"
val DependencyHandler.lifecycleX get() = "androidx.lifecycle:lifecycle-extensions:${V.lifecycleVersion}"
val DependencyHandler.viewModelKtx get() = "androidx.lifecycle:lifecycle-viewmodel-ktx:${V.lifecycleVersion}"
val DependencyHandler.constraintLayout get() = "androidx.constraintlayout:constraintlayout:${V.constraintLayoutVersion}"
val DependencyHandler.preference get() = "androidx.preference:preference:${V.preferenceVersion}"
val DependencyHandler.navigationFragment get() = "android.arch.navigation:navigation-fragment:${V.navVersion}"
val DependencyHandler.material get() = "com.google.android.material:material:${V.materialVersion}"
val DependencyHandler.muzeiApi get() = "com.google.android.apps.muzei:muzei-api:${V.muzeiApiVersion}"
val DependencyHandler.gmsAuth get() = "com.google.android.gms:play-services-auth:${V.gmsAuthVersion}"
val DependencyHandler.workRuntime get() = "android.arch.work:work-runtime-ktx:${V.workManagerVersion}"
val DependencyHandler.retrofit get() = "com.squareup.retrofit2:retrofit:${V.retrofitVersion}"
val DependencyHandler.retrofitMoshi get() = "com.squareup.retrofit2:converter-moshi:${V.retrofitVersion}"
val DependencyHandler.retrofitRx get() = "com.squareup.retrofit2:adapter-rxjava2:${V.retrofitVersion}"
val DependencyHandler.okhttp get() = "com.squareup.okhttp3:okhttp:${V.okhttpVersion}"
val DependencyHandler.picasso get() = "com.squareup.picasso:picasso:${V.picassoVersion}"
val DependencyHandler.picassoTransformations get() = "jp.wasabeef:picasso-transformations:${V.picassoTransformationsVersion}"

// test dependencies
val DependencyHandler.junit get() = "junit:junit:${V.junitVersion}"
val DependencyHandler.testRunner get() = "androidx.test:runner:${V.testRunnerVersion}"
val DependencyHandler.espressoCore get() = "androidx.test.espresso:espresso-core:${V.espressoVersion}"
