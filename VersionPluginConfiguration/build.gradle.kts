plugins {
    kotlin("jvm")
//    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("com.jfrog.artifactory")
    `maven-publish`
    id("maven")
    id("signing")
}

group = "org.daiv.dependency"
version = "0.0.15"


java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
//    maven("https://artifactory.daiv.org/artifactory/gradle-dev-local")
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
}

signing {
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
//            artifactId = "VersionPluginConfiguration"
            from(components["java"])
//            artifact(tasks["javadocJar"])
//            artifact(tasks["sourcesJar"])
            pom {
                packaging = "jar"
                name.set("VersionPluginConfiguration")
                description.set("A versioning plugin for build scripts")
                url.set("https://github.com/henry1986/dependency-versionPlugin")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/henry1986/dependency-versionPlugin/issues")
                }
                scm {
                    connection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
                    developerConnection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
                    url.set("https://github.com/henry1986/dependency-versionPlugin")
                }
                developers {
                    developer {
                        id.set("henry86")
                        name.set("Martin Heinrich")
                        email.set("martin.heinrich.dresden@gmx.de")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatypeRepository"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials(PasswordCredentials::class)
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
            invokeMethod("publications", arrayOf("mavenJava"))
//            invokeMethod("publications",  publishing.publications.names.toTypedArray())
            setProperty("publishPom", true)
            setProperty("publishArtifacts", true)
        })
    })
}

