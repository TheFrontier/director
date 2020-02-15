plugins {
    java
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":director-core"))

    compileOnly("org.spongepowered:spongeapi:7.1.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}