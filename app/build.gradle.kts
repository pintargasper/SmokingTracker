plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "eu.mister3551.smokingtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "eu.mister3551.smokingtracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.apache.poi)
    implementation(libs.apache.poi.ooxml)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
}