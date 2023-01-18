plugins {
    kotlin("multiplatform") version "1.8.255-SNAPSHOT"
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    wasm {
        binaries.executable()
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-wasm"))
            }
        }
    }


}