plugins {
    kotlin("jvm") version "1.9.10"
    application
    `maven-publish`
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-controls:17.0.2")
    implementation("org.openjfx:javafx-fxml:17.0.2")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.1.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(18)
}

application {
    mainClass.set("com.example.lsbsteganography.MainKt")
}

javafx {
    version.set("17.0.2")
    modules.set(listOf("javafx.controls", "javafx.fxml"))
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.example.lsbsteganography.MainKt"
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
tasks.register<org.gradle.api.tasks.javadoc.Javadoc>("javadoc") {
    source = sourceSets["main"].allJava
    classpath = configurations["compileClasspath"]
    destinationDir = file("$buildDir/docs/javadoc")
}

tasks.register("generateDocs") {
    dependsOn("javadoc")
}