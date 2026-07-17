import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.google.services)
}

buildkonfig {
    packageName = "com.pamneuroncraft.jobapplicationtracker"
    objectName = "AppBuildKonfig"
    
    defaultConfigs {
        buildConfigField(BOOLEAN, "FEATURE_AI_IMPORT", "false")
        buildConfigField(BOOLEAN, "FEATURE_GOOGLE_DRIVE_BACKUP", "false")
        
        // Default to Google Test IDs
        buildConfigField(STRING, "ADMOB_APP_ID", "ca-app-pub-9098088729873683~6121804769")
        buildConfigField(STRING, "ADMOB_BANNER_UNIT_ID", "ca-app-pub-9098088729873683/1918835878")
    }
    
    targetConfigs {
        create("paid") {
            buildConfigField(BOOLEAN, "FEATURE_AI_IMPORT", "true")
            buildConfigField(BOOLEAN, "FEATURE_GOOGLE_DRIVE_BACKUP", "true")
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            
            // Using standard Navigation Compose KMP
            implementation(libs.navigation.compose)
            
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.settings)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            
            // Google Drive & Auth (Android Specific for now)
            implementation(libs.google.auth)
            implementation(libs.google.drive)
            implementation(libs.google.api.client.android)
            implementation(libs.google.api.client.gson)
            implementation(libs.google.http.client.gson)
            
            // AI & Scraping
            implementation(libs.generativeai)
            implementation(libs.jsoup)
            implementation(libs.play.services.ads)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.pamneuroncraft.jobapplicationtracker"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pamneuroncraft.jobapplicationtracker"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Pass the App ID from BuildKonfig (or a Gradle property) to the Manifest
        manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"
    }

    flavorDimensions += listOf("tier")
    productFlavors {
        create("free") {
            dimension = "tier"
            applicationIdSuffix = ".free"
        }
        create("paid") {
            dimension = "tier"
            applicationIdSuffix = ".paid"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,DEPENDENCIES}"
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.firebase.bom))
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
