plugins {
    kotlin("jvm")
    id("com.jfrog.artifactory")
    `maven-publish`
}

group = "org.daiv.dependency"
version = "0.0.14"

repositories {
    mavenCentral()
    maven("https://daiv.org/artifactory/gradle-dev-local")
}
dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
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
            invokeMethod("publications",  arrayOf("mavenJava"))
//            invokeMethod("publications",  publishing.publications.names.toTypedArray())
            setProperty("publishPom", true)
            setProperty("publishArtifacts", true)
        })
    })
}

