import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

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
        buildConfigField(BOOLEAN, "FEATURE_AI_IMPORT", "true")
        buildConfigField(BOOLEAN, "FEATURE_GOOGLE_DRIVE_BACKUP", "true")
        buildConfigField(BOOLEAN, "FEATURE_SUMMARY", "true")
        
        // Google Web Client IDs
        buildConfigField(STRING, "GOOGLE_WEB_CLIENT_ID_DEBUG", "587001402052-idcta36ao4seblo39mas8q57vaabi7l9.apps.googleusercontent.com")
        buildConfigField(STRING, "GOOGLE_WEB_CLIENT_ID_RELEASE", "621221034219-noce836ugbop9po122a2mrnsmgtrpf8n.apps.googleusercontent.com")
        
        // RevenueCat API Keys
        buildConfigField(STRING, "REVENUECAT_API_KEY_ANDROID_DEBUG", "test_yIMjzBcWtbQriwjXQEvrlZYHJZN")
        buildConfigField(STRING, "REVENUECAT_API_KEY_ANDROID_RELEASE", "goog_HrWqyESiTxrpzpqsfSlKXUrGmEm")
        buildConfigField(STRING, "REVENUECAT_API_KEY_IOS_DEBUG", "test_yIMjzBcWtbQriwjXQEvrlZYHJZN")
        buildConfigField(STRING, "REVENUECAT_API_KEY_IOS_RELEASE", "appl_placeholder")
        
        // Default to Google Test IDs
        buildConfigField(STRING, "ADMOB_APP_ID", "ca-app-pub-9098088729873683~6121804769")
        buildConfigField(STRING, "ADMOB_BANNER_UNIT_ID", "ca-app-pub-9098088729873683/1918835878")
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true // Required for RevenueCat SDK
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
            implementation(libs.androidx.room.paging)
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
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)
            implementation(libs.purchases.core)
            implementation(libs.uuid)
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
            
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.paging.runtime)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.pamneuroncraft.jobapplicationtracker"
    compileSdk = 37

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

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
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.grpc") {
            // Force a stable version that contains InternalGlobalInterceptors
            useVersion("1.65.1")
        }
    }
}
