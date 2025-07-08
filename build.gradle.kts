import java.net.URLClassLoader

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.classgraph:classgraph:4.8.177")
    }
}

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

    implementation("org.apache.commons:commons-compress:1.27.1")

    implementation("info.picocli:picocli:4.7.7")
    annotationProcessor("info.picocli:picocli-codegen:4.7.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("info.picocli:picocli-jansi-graalvm:1.2.0")
    implementation("org.fusesource.jansi:jansi:2.4.2")

    compileOnly("org.jetbrains:annotations:26.0.2")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
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
        buildArgs.add("--enable-http")
        buildArgs.add("--enable-https")
        buildArgs.add("--enable-native-access=ALL-UNNAMED")
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

        val inputLines = inputFile.readLines().map(String::trim).filter { it.isNotBlank() && !it.startsWith("#") }
        val allClasses = mutableSetOf<String>()

        val scanResult = io.github.classgraph.ClassGraph()
            .overrideClassLoaders(classLoader)
            .enableAllInfo()
            .scan()

        inputLines.forEach { line ->
            if (line.endsWith(".*") || line.endsWith("]")) {
                val packageName = line.substringBeforeLast(".^[", "").ifBlank { line.substringBeforeLast(".[", "").ifBlank { line.substringBeforeLast(".") } }
                val classesInPackage = scanResult.getPackageInfo(packageName)?.classInfo ?: emptyList()

                val filter: (String) -> Boolean = if (line.contains("[")) {
                    val regexes = line.substringAfter("[").substringBefore("]").split(",").map(String::trim).map(String::toRegex)

                    if (line.contains("^[")) {
                        { className -> regexes.none { className.matches(it) } }
                    } else {
                        { className -> regexes.any { className.matches(it) } }
                    }
                } else {
                    { true }
                }

                for (classInfo in classesInPackage) {
                    if (!classInfo.isInterface && !classInfo.isAbstract && filter(classInfo.name.substringAfterLast('.'))) {
                        allClasses.add(classInfo.name)
                    }
                }
            } else {
                allClasses.add(line)
            }
        }

        scanResult.close()

        allClasses.forEach { className ->
            try {
                Class.forName(className, false, classLoader)
            } catch (_: ClassNotFoundException) {
                throw GradleException("Invalid class '$className' found in reflected.txt")
            }
        }

        val reflectionConfig = allClasses.map {
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
