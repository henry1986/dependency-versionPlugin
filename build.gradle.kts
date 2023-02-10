import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
    id("com.jfrog.artifactory") version "4.17.2"
    `maven-publish`
    `java-gradle-plugin`
    id("maven")
    id("signing")
}


gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "org.daiv.dependency.VersionsPlugin"
            implementationClass = "org.daiv.dependency.VersionPlugin"
            description = ""

        }
    }
}


group = "org.daiv.dependency"
version = "0.1.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation(project(":VersionPluginConfiguration"))
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
}
java {
    withJavadocJar()
    withSourcesJar()
}
signing {
    sign(publishing.publications)
}
val jarName = "VersionPlugin"
publishing {
    publications {
//        create<MavenPublication>("pluginMaven") {
////            artifactId = "VersionPluginConfiguration"
//            from(components["java"])
////            artifact(tasks["javadocJar"])
////            artifact(tasks["sourcesJar"])
//            pom {
//                packaging = "jar"
//                name.set(jarName)
//                description.set("A versioning plugin for build scripts")
//                url.set("https://github.com/henry1986/dependency-versionPlugin")
//                licenses {
//                    license {
//                        name.set("The Apache Software License, Version 2.0")
//                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                    }
//                }
//                issueManagement {
//                    system.set("Github")
//                    url.set("https://github.com/henry1986/dependency-versionPlugin/issues")
//                }
//                scm {
//                    connection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
//                    developerConnection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
//                    url.set("https://github.com/henry1986/dependency-versionPlugin")
//                }
//                developers {
//                    developer {
//                        id.set("henry86")
//                        name.set("Martin Heinrich")
//                        email.set("martin.heinrich.dresden@gmx.de")
//                    }
//                }
//            }
//        }
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

afterEvaluate {
    tasks.withType<GenerateMavenPom> {
        val pom = this.pom
        doFirst {
            println("pom: ${pom.name.isPresent}")
            if(!pom.name.isPresent && pom.packaging == "pom") {
                println("hello pom")
                pom.name.set("VersionsPlugin")
                pom.description.set("A versioning plugin for build scripts")
                pom.url.set("https://github.com/henry1986/dependency-versionPlugin")
                pom.licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                pom.issueManagement {
                    system.set("Github")
                    url.set("https://github.com/henry1986/dependency-versionPlugin/issues")
                }
                pom.scm {
                    connection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
                    developerConnection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
                    url.set("https://github.com/henry1986/dependency-versionPlugin")
                }
                pom.developers {
                    developer {
                        id.set("henry86")
                        name.set("Martin Heinrich")
                        email.set("martin.heinrich.dresden@gmx.de")
                    }
                }
            } else {
                pom.name.set("VersionPlugin")
                pom.description.set("A versioning plugin for build scripts")
                pom.url.set("https://github.com/henry1986/dependency-versionPlugin")
                pom.licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                pom.issueManagement {
                    system.set("Github")
                    url.set("https://github.com/henry1986/dependency-versionPlugin/issues")
                }
                pom.scm {
                    connection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
                    developerConnection.set("scm:git:https://github.com/henry1986/dependency-versionPlugin.git")
                    url.set("https://github.com/henry1986/dependency-versionPlugin")
                }
                pom.developers {
                    developer {
                        id.set("henry86")
                        name.set("Martin Heinrich")
                        email.set("martin.heinrich.dresden@gmx.de")
                    }
                }
            }
        }
    }
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
            invokeMethod("publications", publishing.publications.names.toTypedArray())
            setProperty("publishPom", true)
            setProperty("publishArtifacts", true)
        })
    })
}
