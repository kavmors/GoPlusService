plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.kavmors.goplus"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.kavmors.goplus"
        minSdk = 28
        targetSdk = 33
        versionCode = 101
        versionName = "1.0.1"

        ndk {
            abiFilters.add("armeabi-v7a")
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = "goplus"
            keyPassword = "123456"
            storeFile = file("$rootDir/release.keystore")
            storePassword = "123456"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.yserialport)
}
