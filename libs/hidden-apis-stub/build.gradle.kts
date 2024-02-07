plugins {
    id("com.android.library")
}

android {
    compileSdk = 31
    namespace = "im.angry.hidden.apis"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

dependencies {
    implementation("org.jetbrains:annotations:15.0")
}