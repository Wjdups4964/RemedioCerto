plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

// Firebase (RNF17): o google-services.json não é versionado. O plugin só é aplicado
// quando o arquivo existe em app/, assim o projeto sincroniza e compila antes de
// você configurar o Firebase. Veja o README para baixar o arquivo do console.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.example.remediocerto"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.remediocerto"
        minSdk = 26 // Android 8.0 (RNF19)
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
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

// Room: exporta o esquema do banco (versionar a pasta app/schemas)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Base e interface (Views/XML)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // MVVM (RNF22)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navegação entre Fragments + navegação inferior
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Dados locais (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Segundo plano
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // bulário-api (HTTPS, timeout de 10 s — RNF10, RNF16)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    // Firebase: Auth e Firestore (versões definidas pelo BoM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
