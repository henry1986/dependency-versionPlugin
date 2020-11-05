package org.daiv.dependency

import com.google.gson.GsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.tooling.GradleConnector
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.reflect.KClass

open class VersionPluginExtensions<T:Any> {
    lateinit var clazz: KClass<T>
    var resetVersion: T.() -> T = { this }
    var versionKey = "dependencyHandlingVersion"
    var publishTaskName = "artifactoryPublish"
    var cleanTaskName = "clean"
    var dependencyHandlingProperties = "dependencyHandling.properties"
    var dependencyHandlingPropertyPath = "path"
    var versionsJsonPath = "src/main/resources/versions.json"
    var versionsProperties = "versions.properties"

    fun getProjectDir(project: Project): String {
        val p = java.util.Properties()
        p.load(FileReader(project.file(dependencyHandlingProperties)))
        return p.getProperty(dependencyHandlingPropertyPath)
    }
}

data class VersionPluginConfiguration<T:Any>(
    val projectDir: String,
    val configure: VersionPluginExtensions<T>,
    val project: Project
) {
    private fun projectDirFile() = project.file(projectDir)

    fun publish() {
        val connector = GradleConnector.newConnector()
        connector.forProjectDirectory(projectDirFile())
        val connection = connector.connect()
        val build = connection.newBuild()
        build.forTasks(*(listOf(configure.cleanTaskName, configure.publishTaskName).toTypedArray()))
        build.run()
        connection.close()
    }

    fun resetDependencyHandlingVersion() {
        val prop = Properties()
        val versionPropertiesFile = project.file("${projectDirFile().path}/${configure.versionsProperties}")
        prop.load(FileReader(versionPropertiesFile))
        val version = prop.getProperty(configure.versionKey)
        prop.setProperty(configure.versionKey, version.increment())
        prop.store(FileWriter(versionPropertiesFile), null)
    }

    private fun<T:Any> versions(
        json: String = String::class.java.classLoader.getResource("versions.json").readText(),
        clazz:KClass<T>
    ): T {
        val gson = GsonBuilder().setPrettyPrinting()
            .create()
        return gson.fromJson(json, clazz.java)!!
    }

    fun resetMyVersion() {
        val versionsFile = project.file("$projectDir/${configure.versionsJsonPath}")

        val json = versionsFile.readText()
        val versions = versions(json, configure.clazz)

        val resetVersion = configure.resetVersion
        versions.resetVersion().store(versionsFile)
    }
}

internal fun String.increment(): String {
    val split = split(".")
    val last = (split[split.size - 1]).toInt() + 1
    val drop = split.dropLast(1)
    return drop.joinToString(".") + ".$last"
}


fun <T:Any> T.increment(func: T.() -> String): String {
    return func().increment()
}

internal fun <T : Any> T.store(file: File) {
    val gson = GsonBuilder().setPrettyPrinting()
        .create()
    val x = gson.toJson(this)
    val fw = FileWriter(file)
    fw.write(x)
    fw.close()
}


class VersionsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.task("install") { task: Task ->
            val configure = target.extensions.create("resetVersion", VersionPluginExtensions::class.java)
            task.doLast {
                val versionPluginConfiguration =
                    VersionPluginConfiguration(configure.getProjectDir(target), configure, target)
                versionPluginConfiguration.resetMyVersion()
                versionPluginConfiguration.publish()
                versionPluginConfiguration.resetDependencyHandlingVersion()
            }
        }
    }
}
