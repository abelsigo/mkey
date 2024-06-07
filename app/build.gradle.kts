import org.apache.tools.ant.util.JavaEnvUtils.VERSION_1_8

plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.abel.mkey"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.abel.mkey"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        exclude ("META-INF/INDEX.LIST")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.google.api-client:google-api-client:2.0.0")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation ("com.google.api-client:google-api-client-android:1.20.0")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation("org.passay:passay:1.6.4")
    implementation ("com.google.android.gms:play-services-drive:17.0.0")
    implementation ("com.google.apis:google-api-services-drive:v3-rev20240327-2.0.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    implementation ("com.google.http-client:google-http-client-jackson2:1.39.0")
    implementation ("com.google.guava:guava:24.1-jre")
// Guava fix
    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation ("com.squareup.picasso:picasso:2.8")

}