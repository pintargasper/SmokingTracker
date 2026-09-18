import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    id("com.github.jk1.dependency-license-report") version "3.1.4"
}

kotlin {
    jvmToolchain(jdkVersion = 21)
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xjvm-default=all"))
    }
}

configure<ApplicationExtension> {
    namespace = "com.gasperpintar.smokingtracker"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.gasperpintar.smokingtracker"
        minSdk = 26
        targetSdk = 37
        versionCode = 15
        versionName = "1.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (providers.gradleProperty("KEYSTORE_FILE").isPresent) {
                storeFile = file(providers.gradleProperty("KEYSTORE_FILE").get())
                storePassword = providers.gradleProperty("KEYSTORE_PASSWORD").orNull
                keyAlias = providers.gradleProperty("KEY_ALIAS").orNull
                keyPassword = providers.gradleProperty("KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            if (providers.gradleProperty("KEYSTORE_FILE").isPresent) {
                signingConfig = signingConfigs.getByName("release")
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("ObsoleteSdkInt")
        disable.add("TrustAllX509TrustManager")
        disable.add("TooManyViews")
    }

    sourceSets {
        getByName("main") {
            assets.directories.add(
                layout.buildDirectory.dir(
                    "versions/v${defaultConfig.versionName}"
                ).get().asFile.path
            )
        }
    }
}

tasks {
    copyVersionFiles()
}

fun copyVersionFiles() {
    val version = "v${android.defaultConfig.versionName}"
    val source = rootProject.file("versions/$version")
    val destination = layout.buildDirectory.dir("versions/$version")

    tasks.register<Copy>(name = "copyVersionFiles") {
        description = "Copies version files to the package assets"
        from(source)
        into(destination)
    }

    tasks.named("preBuild") {
        dependsOn("copyVersionFiles")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.poi)
    implementation(libs.poi.ooxml)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.screengrab)
    androidTestImplementation(libs.androidx.espresso.contrib)
}