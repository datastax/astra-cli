import java.net.URLClassLoader

plugins {
    java
    application
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "com.dtsx.astra.cli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-nop:2.0.17")

    implementation("com.datastax.astra:astra-db-java:2.0.0")
    implementation("com.datastax.astra:astra-sdk-devops:1.2.9")

    implementation("info.picocli:picocli:4.7.7")
    annotationProcessor("info.picocli:picocli-codegen:4.7.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.jetbrains:annotations:26.0.2")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:2.19.0")
}

tasks.compileJava {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

application {
    mainClass.set("$group.AstraCli")
}

graalvmNative {
    binaries.all {
        buildArgs.add("-Os")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("generateGraalReflectionConfig") {
    val inputFile = file("reflected.txt")

    val outputDir = file("${layout.buildDirectory.get()}/classes/java/main/META-INF/native-image/astra-cli-generated/${project.group}/${project.name}")
    val outputFile = file("${outputDir}/reflect-config.json")

    inputs.file(inputFile)
    outputs.file(outputFile)

    doLast {
        outputDir.mkdirs()

        val classpath = sourceSets.main.get().runtimeClasspath
        val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())

        val classes = inputFile.readLines().map(String::trim).filter { it.isNotBlank() && !it.startsWith("#") }

        classes.forEach {
            try {
                Class.forName(it, false, classLoader)
            } catch (_: ClassNotFoundException) {
                throw GradleException("Invalid class '$it' found in reflected.txt")
            }
        }

        val reflectionConfig = classes.map {
            mapOf(
                "name" to it,
                "allDeclaredConstructors" to true,
                "allPublicConstructors" to true,
                "allDeclaredMethods" to true,
            )
        }

        val json = groovy.json.JsonBuilder(reflectionConfig).toString()
        outputFile.writeText(json)
    }
}

tasks.jar {
    dependsOn("generateGraalReflectionConfig")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
