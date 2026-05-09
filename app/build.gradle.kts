import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

val today: String = SimpleDateFormat("yyyyMMdd").format(Date())
val counterFile = file("../debug-build-counter.txt")
val debugBuildNumber: Int = run {
    val lines = if (counterFile.exists()) counterFile.readLines() else emptyList()
    val storedDate = lines.getOrNull(0)?.trim()
    val storedCount = lines.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    val count = if (storedDate == today) storedCount + 1 else 1
    counterFile.writeText("$today\n$count")
    count
}
val debugLabel: String = "$today-$debugBuildNumber"

android {
    namespace = "com.jmp.pocketmoneyapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jmp.pocketmoneyapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "2.02.000"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "PocketMoney2026!"
            keyAlias = "pocketmoneyapp"
            keyPassword = "PocketMoney2026!"
        }
    }

    buildTypes {
        debug {
            // Temporarily disabled to avoid Firebase configuration issues
            // applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG-$debugLabel"
            firebaseAppDistribution {
                appId = "1:790017941760:android:16b72dcd008493bc9cd3e5"
                releaseNotes = "Debug build - internal test only"
                testers = "jacobflop@gmail.com"
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            firebaseAppDistribution {
                appId = "1:790017941760:android:16b72dcd008493bc9cd3e5"
                releaseNotesFile = "release-notes.txt"
                groups = "Familien"
            }
        }
    }

    lint {
        abortOnError = false
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.0")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Credential Manager (Google Password Manager integration)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.1.0")
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Drag-to-reorder in LazyColumn
    implementation("sh.calvin.reorderable:reorderable:2.4.3")

    // Spotlight / intro tour
    implementation("com.canopas.intro-showcase-view:introshowcaseview:2.0.2")

    // DataStore for local preferences (chore sort order)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // QR Code generation and scanning
    implementation("com.google.zxing:core:3.5.3")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Firebase BOM
    val firebaseBom = platform("com.google.firebase:firebase-bom:33.1.0")
    implementation(firebaseBom)
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Firebase App Distribution — in-app tester feedback
    // API-only in all variants (no-op in release); full SDK in debug only
    implementation("com.google.firebase:firebase-appdistribution-api-ktx:16.0.0-beta15")
    debugImplementation("com.google.firebase:firebase-appdistribution:16.0.0-beta18")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
