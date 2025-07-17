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
version = "1.0.0-alpha.0"

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
    implementation("info.picocli:picocli-codegen:4.7.7")

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
    mainClass.set("com.dtsx.astra.cli.AstraCli")
}

val isProd = project.hasProperty("prod")

val cliSystemProperties = mapOf(
    "cli.version" to project.version.toString(),
    "cli.rc-file-name" to if (isProd) ".astrarc" else ".astrarc-dev",
)

graalvmNative {
    binaries.all {
        imageName.set("astra")

        buildArgs.add("--enable-http")
        buildArgs.add("--enable-https")
        buildArgs.add("--enable-native-access=ALL-UNNAMED")
        
        if (project.hasProperty("prod")) {
            buildArgs.add("-Os")
        } else {
            buildArgs.add("-Ob")
        }
    }
}

initNativeArchiveTask<Tar>("nativeTar") {
    compression = Compression.GZIP
    archiveExtension = "tar.gz"

    from(tasks.nativeCompile.get().outputs.files.singleFile) {
        filePermissions {
            unix("rwxr-xr-x")
        }
    }
}

initNativeArchiveTask<Zip>("nativeZip") {
    from(tasks.nativeCompile.get().outputs.files) {
        include("*.exe")
    }
}

inline fun <reified T : AbstractArchiveTask>initNativeArchiveTask(name: String, crossinline otherConfiguration: T.() -> Unit) {
    tasks.register<T>(name) {
        dependsOn("nativeCompile")
        archiveBaseName.set("astra")
        archiveVersion.set("")
        archiveClassifier.set(getOsArch())
        destinationDirectory.set(file("${layout.buildDirectory.get()}/distributions"))
        otherConfiguration()
    }
}

tasks.test {
    useJUnitPlatform()
}

val nativeImageGeneratedDir = layout.buildDirectory.dir("classes/java/main/META-INF/native-image/astra-cli-generated/${project.group}/${project.name}").get().asFile

tasks.register<JavaExec>("generateJniConfig") {
    val outputFile = file("${nativeImageGeneratedDir}/jni-config.json")

    doFirst {
        outputFile.parentFile.mkdirs()
    }

    mainClass.set("picocli.codegen.aot.graalvm.JniConfigGenerator")

    classpath = files((configurations.runtimeClasspath.get() + configurations.annotationProcessor.get()).filter {
        it.name.startsWith("picocli") || it.name.startsWith("jansi")
    })

    args = listOf(
        "org.fusesource.jansi.internal.CLibrary",
        "org.fusesource.jansi.internal.Kernel32",
        "-o=${outputFile.absolutePath}"
    )

    jvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED"
    )

    outputs.file(outputFile)
}

tasks.register("generateGraalReflectionConfig") {
    val inputFile = file("reflected.txt")

    val outputFile = file("${nativeImageGeneratedDir}/reflect-config.json")

    inputs.file(inputFile)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()

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

val resourcePatterns = mutableListOf<String>()

tasks.register("includeJansiNativeLibResources") {
    val (os, arch) = getOsArch().split("-")

    val osPattern = when (os) {
        "windows" -> "Windows"
        "macos" -> "MacOS"
        "linux" -> "Linux"
        else -> ".*"
    }

    val archPattern = when (arch) {
        "x86_64", "arm64" -> arch
        else -> ".*"
    }

    resourcePatterns.add(
        "\\QMETA-INF/native-image/org/fusesource/jansi/$osPattern/$archPattern/.*\\E"
    )
}

tasks.register("createDynamicProperties") {
    val outputFile = layout.buildDirectory.file("resources/main/dynamic.properties").get().asFile

    outputs.file(outputFile)

    doLast {
        val output = cliSystemProperties.map { (key, value) -> "$key=$value" }.joinToString("\n")

        outputFile.parentFile.mkdirs()
        outputFile.writeText(output);

        resourcePatterns.add(
            "\\Qdynamic.properties\\E"
        )
    }
}

tasks.register("generateGraalResourceConfig") {
    dependsOn("includeJansiNativeLibResources")
    dependsOn("createDynamicProperties")

    val outputFile = file("${nativeImageGeneratedDir}/resource-config.json")

    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()

        val resourceConfig = mapOf(
            "resources" to mapOf(
                "includes" to resourcePatterns.map { mapOf("pattern" to it) }
            ),
            "bundles" to emptyList<Any>()
        )

        val json = groovy.json.JsonBuilder(resourceConfig).toString()
        outputFile.writeText(json)
    }
}

tasks.jar {
    dependsOn("generateGraalReflectionConfig")
    dependsOn("generateGraalResourceConfig")
    dependsOn("generateJniConfig")
}

tasks.named<JavaExec>("run") {
    dependsOn("createDynamicProperties")
    standardInput = System.`in`
}

fun getOsArch(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()

    return when {
        os.contains("windows") -> {
            if (arch.contains("aarch64") || arch.contains("arm")) "windows-arm64" else "windows-x86_64"
        }
        os.contains("mac") || os.contains("darwin") -> {
            if (arch.contains("aarch64") || arch.contains("arm")) "macos-arm64" else "macos-x86_64"
        }
        os.contains("linux") -> {
            if (arch.contains("aarch64") || arch.contains("arm")) "linux-arm64" else "linux-x86_64"
        }
        else -> throw GradleException("Unsupported OS: $os with architecture $arch")
    }
}

tasks.register<Jar>("fatJar") {
    dependsOn(configurations.runtimeClasspath)
    archiveBaseName.set("fat")
    archiveVersion.set(project.version.toString())

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.dtsx.astra.cli.AstraCli"
    }

    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
