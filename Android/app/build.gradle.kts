import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the ksp plugin when using Room
    id("com.google.devtools.ksp")
}

val properties = Properties()
val inputStream = project.rootProject.file("local.properties").inputStream()
properties.load( inputStream )

android {
    namespace = "com.hyphenate.scenarios"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hyphenate.scenarios"
        minSdk = 24
        targetSdk = 34
        versionCode = 100
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Set app server info from local.properties
        buildConfigField ("String", "APP_SERVER_PROTOCOL", "\"https\"")
        buildConfigField ("String", "APP_SERVER_DOMAIN", "\"${properties.getProperty("APP_SERVER_DOMAIN")}\"")
        buildConfigField ("String", "APP_SERVER_LOGIN", "\"${properties.getProperty("APP_SERVER_LOGIN")}\"")
        buildConfigField ("String", "APP_BASE_USER", "\"${properties.getProperty("APP_BASE_USER")}\"")
        buildConfigField ("String", "APP_USER_MATCH", "\"${properties.getProperty("APP_USER_MATCH")}\"")
        buildConfigField ("String", "APP_MATCH_STATUS", "\"${properties.getProperty("APP_MATCH_STATUS")}\"")
        buildConfigField ("String", "APP_UPLOAD_AVATAR", "\"${properties.getProperty("APP_UPLOAD_AVATAR")}\"")
        buildConfigField ("String", "APP_SEND_SMS_FROM_SERVER", "\"${properties.getProperty("APP_SEND_SMS_FROM_SERVER")}\"")
        buildConfigField ("String", "APP_RTC_TOKEN_URL", "\"${properties.getProperty("APP_RTC_TOKEN_URL")}\"")
        buildConfigField ("String", "APP_RTC_CHANNEL_MAPPER_URL", "\"${properties.getProperty("APP_RTC_CHANNEL_MAPPER_URL")}\"")

        // Set appkey from local.properties
        buildConfigField("String", "APPKEY", "\"${properties.getProperty("APPKEY")}\"")
        // Set RTC appId from local.properties
        buildConfigField("String", "RTC_APPID", "\"${properties.getProperty("RTC_APPID")}\"")

        //指定room.schemaLocation生成的文件路径  处理Room 警告 Schema export Error
        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                ))
            }
        }

        ndk {
            abiFilters .addAll(mutableSetOf("arm64-v8a","armeabi-v7a"))
        }

        //用于设置使用as打包so时指定输出目录
        externalNativeBuild {
            ndkBuild {
                abiFilters("arm64-v8a","armeabi-v7a")
                arguments("-j8")
            }
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(properties.getProperty("DEBUG_STORE_FILE_PATH", "./keystore/sdkdemo.jks"))
            storePassword = properties.getProperty("DEBUG_STORE_PASSWORD", "123456")
            keyAlias = properties.getProperty("DEBUG_KEY_ALIAS", "easemob")
            keyPassword = properties.getProperty("DEBUG_KEY_PASSWORD", "123456")
        }
        create("release") {
            storeFile = file(properties.getProperty("RELEASE_STORE_FILE_PATH", "./keystore/sdkdemo.jks"))
            storePassword = properties.getProperty("RELEASE_STORE_PASSWORD", "123456")
            keyAlias = properties.getProperty("RELEASE_KEY_ALIAS", "easemob")
            keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD", "123456")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
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

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }

    dataBinding{
        enable = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-material:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("pub.devrel:easypermissions:3.0.0")
    // image corp library
    implementation("com.github.yalantis:ucrop:2.2.8")
    // Coil: load image library
    implementation("io.coil-kt:coil:2.5.0")
    // lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    // lifecycle viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // coroutines core library
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("androidx.annotation:annotation:1.1.0")
    // Room
    implementation("androidx.room:room-runtime:2.5.1")
    ksp("androidx.room:room-compiler:2.5.1")
    // optional - Kotlin Extensions and Coroutines support for Room
    // To use Kotlin Flow and coroutines with Room, must include the room-ktx artifact in build.gradle file.
    implementation("androidx.room:room-ktx:2.5.1")
    implementation("androidx.databinding:databinding-runtime:4.2.2")
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("io.hyphenate:ease-chat-kit:4.8.2")
    implementation("io.agora.rtc:full-rtc-basic:4.1.0")

    implementation("com.tencent.tav:libpag:latest.release")
}