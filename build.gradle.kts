plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.nexus.publish)
    base
}

group = "me.tatarka.inject"
version = libs.versions.kotlin.inject.get()

nexusPublishing {
    repositories {
        sonatype()
    }
}

val testReport by tasks.registering(TestReport::class) {
    destinationDir = buildDir.resolve("reports")
}

val copyTestResults by tasks.registering(Copy::class) {
    destinationDir = buildDir.resolve("test-results")
    includeEmptyDirs = false
}

val testReportApple by tasks.registering(TestReport::class) {
    destinationDir = buildDir.resolve("reports")
}

val copyTestResultsApple by tasks.registering(Copy::class) {
    destinationDir = buildDir.resolve("test-results")
    includeEmptyDirs = false
}

val check by tasks.getting
val checkApple by tasks.creating

check.finalizedBy(testReport, copyTestResults)
checkApple.finalizedBy(testReportApple, copyTestResultsApple)