plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.obk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.obk"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    /* ───── AndroidX 基础 UI ───── */
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    /* ───── Lifecycle / LiveData / ViewModel ───── */
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    /* ───── Room 本地数据库 ───── */
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    /* ───── WorkManager（后台任务 & 离线同步）───── */
    implementation("androidx.work:work-runtime:2.9.0")

    /* ───── Retrofit + Gson (假 API / 未来真后端) ───── */
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    /* ───── ZXing 扫码 ───── */
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    /* ───── CameraX 拍照 ───── */
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")

    /* ───── Glide 显示图片/缩略图 ───── */
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    /* ───── 测试 ───── */
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.android.material:material:1.11.0")

    /* ───── CameraX 拍照 ───── */
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)

    implementation (libs.logging.interceptor)
    implementation (libs.retrofit2.converter.gson)
    implementation (libs.squareup.retrofit)
    implementation (libs.bcprov.jdk15to18)

    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")


}