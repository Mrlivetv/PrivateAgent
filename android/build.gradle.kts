allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}

subprojects {
    project.evaluationDependsOn(":app")
}

// ==================================================================
// JVM target alignment fix
// ------------------------------------------------------------------
// Plugins like `shizuku_api` compile against JVM 11 while this app
// uses JVM 17, which Gradle 8+ rejects with:
//   "Inconsistent JVM-target compatibility detected for tasks
//    'compileDebugJavaWithJavac' (11) and 'compileDebugKotlin' (17)"
//
// We hook into plugin application (not afterEvaluate, which would
// throw "Project.afterEvaluate(Action) when the project is already
// evaluated" because of the evaluationDependsOn call above).
// ==================================================================
subprojects {
    // Fires as soon as the Android plugin is applied — safe here.
    plugins.withId("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
    plugins.withId("com.android.application") {
        extensions.configure<com.android.build.gradle.AppExtension>("android") {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    // Fires as soon as the Kotlin plugin is applied — safe here too.
    plugins.withId("org.jetbrains.kotlin.android") {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
            .configureEach {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
