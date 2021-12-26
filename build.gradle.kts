import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
    id("com.jfrog.artifactory") version "4.17.2"
    `maven-publish`
    `java-gradle-plugin`
}


gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "org.daiv.dependency.VersionsPlugin"
            implementationClass = "org.daiv.dependency.VersionsPlugin"
        }
    }
}


group = "org.daiv.dependency"
version = "0.1.3"

repositories {
    mavenCentral()
    maven("https://artifactory.daiv.org/artifactory/gradle-dev-local")
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
}

dependencies {
    implementation("org.gradle:gradle-tooling-api:6.7")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation(project(":VersionPluginConfiguration"))
    implementation("org.daiv.dependency:DependencyHandling:0.0.165")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

artifactory {
    setContextUrl("${project.findProperty("daiv_contextUrl")}")
    publish(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig> {
        repository(delegateClosureOf<groovy.lang.GroovyObject> {
            setProperty("repoKey", "gradle-dev-local")
            setProperty("username", project.findProperty("daiv_user"))
            setProperty("password", project.findProperty("daiv_password"))
            setProperty("maven", true)
        })

        defaults(delegateClosureOf<groovy.lang.GroovyObject> {
//            invokeMethod("publications",  arrayOf("mavenJava"))
            invokeMethod("publications",  publishing.publications.names.toTypedArray())
            setProperty("publishPom", true)
            setProperty("publishArtifacts", true)
        })
    })
}
