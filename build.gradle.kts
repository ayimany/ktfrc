import edu.wpi.first.deployutils.deploy.artifact.FileTreeArtifact
import edu.wpi.first.gradlerio.GradleRIOPlugin
import edu.wpi.first.gradlerio.deploy.roborio.FRCJavaArtifact
import edu.wpi.first.gradlerio.deploy.roborio.RoboRIO
import edu.wpi.first.toolchain.NativePlatforms.desktop
import edu.wpi.first.toolchain.NativePlatforms.roborio
import org.gradle.internal.os.OperatingSystem
import java.lang.System.getProperty
import java.lang.System.getenv
import java.net.URI
import java.util.*
import kotlin.io.path.Path

//
// Plugin Definitions
//

plugins {
    kotlin("jvm") version "2.1.10"
    id("java")
    id("edu.wpi.first.GradleRIO") version "2025.3.2"
}

//
// Variable Definitions
//

group = "net.ayimany"
version = "0.0.0"

val defaultTeamNumber = 0
val mainClass = "net.frc.RobotEntrypointKt.main"
val localDeployDirectory = "resources/deploy"

//
// Target constants
//

val roboRIOTargetName = "roborio"
val roborioDebugDependencyHandlerName = "roborioDebug"
val roborioReleaseDependencyHandlerName = "roborioRelease"
val frcJavaArtifactName = "frcJava"
val frcStaticFileDeployArtifactName = "frcStaticFileDeploy"
val roborioDeployDirectory = "/home/lvuser/deploy"

//
// Configuration
//

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven(wpilibLocal())
}

//
// VM config
//

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

kotlin {
    jvmToolchain(23)
}

//
// GradleRIO
//

deploy {
    targets.register(roboRIOTargetName, RoboRIO::class) {
        team = project.frc.getTeamOrDefault(defaultTeamNumber)
        debug = project.frc.getDebugOrDefault(false)
    }

    targets[roboRIOTargetName].artifacts.create(frcJavaArtifactName, FRCJavaArtifact::class) {}
    targets[roboRIOTargetName].artifacts.create(frcStaticFileDeployArtifactName, FileTreeArtifact::class) {
        files = fileTree(localDeployDirectory)
        directory = roborioDeployDirectory
        deleteOldFiles = true
    }
}

val frcJavaArtifact = deploy.targets[roboRIOTargetName].artifacts[frcJavaArtifactName] as FRCJavaArtifact

//
// Deps
//

dependencies {
    testImplementation(kotlin("test"))

    wpi.java.deps.wpilibAnnotations().forEach { annotationProcessor(it) }

    wpi.java.deps.wpilib().forEach(::implementation)
    wpi.java.vendor.java().forEach(::implementation)

    wpi.java.deps.wpilibJniDebug(roborio).forEach{ add(roborioDebugDependencyHandlerName, it) }
    wpi.java.vendor.jniDebug(roborio).forEach{ add(roborioDebugDependencyHandlerName, it) }

    wpi.java.deps.wpilibJniRelease(roborio).forEach { add(roborioReleaseDependencyHandlerName, it) }
    wpi.java.vendor.jniRelease(roborio).forEach { add(roborioReleaseDependencyHandlerName, it) }

    wpi.java.deps.wpilibJniDebug(desktop).forEach(::nativeDebug)
    wpi.java.vendor.jniDebug(desktop).forEach(::nativeDebug)
    wpi.sim.enableDebug().forEach(::simulationDebug)

    wpi.java.deps.wpilibJniRelease(desktop).forEach(::nativeRelease)
    wpi.java.vendor.jniRelease(desktop).forEach(::nativeRelease)
    wpi.sim.enableRelease().forEach(::simulationRelease)
}

//
// Tasks
//

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(sourceSets.main.get().output)
    manifest(GradleRIOPlugin.javaManifest(mainClass))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

frcJavaArtifact.setJarTask(tasks.jar)
wpi.java.configureExecutableTasks(tasks.jar.get())
wpi.java.configureTestTasks(tasks.test.get())

tasks.withType(JavaCompile::class) {
    options.compilerArgs.add("-XDstringConcat=inline")
}


tasks.test {
    useJUnitPlatform()
}

//
// WPI Lib
//

wpi.sim.addGui().defaultEnabled = true
wpi.sim.addDriverstation()

//
// Miscellaneous
//

val properties: Properties = System.getProperties()
properties.setProperty("org.gradle.internal.native.headers.unresolved.dependencies.ignore", "true")

//
// Helpers
//

fun getPublicRoot(): String {
    val os = OperatingSystem.current()
    return if (os.isWindows) getenv("PUBLIC") ?: "C:\\Users\\Public"
    else getProperty("user.home")!!
}

fun wpilibLocal(): URI {
    val year = "2025"
    val publicRoot = getPublicRoot()
    return Path(publicRoot, "wpilib", year, "maven").toUri()
}
