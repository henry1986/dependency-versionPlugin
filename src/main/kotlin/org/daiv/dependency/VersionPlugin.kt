package org.daiv.dependency

import com.google.gson.GsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.tooling.GradleConnector
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.reflect.KClass


data class VersionPluginConfiguration<T : Any>(
    val projectDir: String,
    val configure: VersionPluginExtension<T>,
    val project: Project
) {
    private fun projectDirFile() = project.file(projectDir)
    val dependencyHandlingData = DependencyHandlingData()

    fun publishDependencyHandling() {
        val connector = GradleConnector.newConnector()
        connector.forProjectDirectory(projectDirFile())
        val connection = connector.connect()
        val build = connection.newBuild()
        build.forTasks(*(listOf(configure.cleanTaskName, configure.publishTaskName).toTypedArray()))
        build.run()
        connection.close()
    }

    inner class DependencyHandlingData() {
        val prop: Properties = Properties()
        val versionPropertiesFile = project.file("${projectDirFile().path}/${configure.versionsProperties}")

        init {
            prop.load(FileReader(versionPropertiesFile))
        }

        val version by lazy { prop.getProperty(configure.versionKey) }
        val nextVersion by lazy { this.version!!.incrementVersion() }

        fun resetDependencyHandlingVersion(): String {
            prop.setProperty(configure.versionKey, nextVersion)
            prop.store(FileWriter(versionPropertiesFile), null)
            return nextVersion
        }
    }

    private fun <T : Any> readJson(
        json: String = String::class.java.classLoader.getResource("versions.json").readText(),
        clazz: KClass<T>
    ): T {
        val gson = GsonBuilder().setPrettyPrinting()
            .create()
        return gson.fromJson(json, clazz.java)!!
    }

    fun resetMyVersion(): T {
        val versionJsonPath =
            "${configure.versionJsonResourcePath}/${configure.versionJsonPath}/${configure.versionJsonName}"
        val versionsFile = project.file("$projectDir/${versionJsonPath}")

        val json = versionsFile.readText()
        val versions = readJson(json, configure.clazz)
        val resetVersion = configure.resetVersion
        val newVersions = versions.resetVersion(versions.incrementVersion { configure.versionMember(versions) })
        val member = configure.versionMember(newVersions)
        if (project.version != member) {
            throw Exception(
                """     member is not fitting: project.version: ${project.version} vs versionMember: $member - 
                        Did you in your build.gradle.kts set version by:

                            version = versions.setVersion { ${project.name} } ?
                            
                        Or maybe you should set in your buildscript classpath
                        your DependencyHandling version to ${dependencyHandlingData.version}?"""
            )
        }
        newVersions.store(versionsFile)
        return newVersions
    }
}

internal fun <T : Any> T.store(file: File) {
    val gson = GsonBuilder().setPrettyPrinting()
        .create()
    val x = gson.toJson(this)
    val fw = FileWriter(file)
    fw.write(x)
    fw.close()
}

internal fun <T : Any> VersionPluginExtension<T>.getProjectDir(project: Project): String {
    val p = Properties()
    p.load(FileReader(project.file(dependencyHandlingProperties)))
    return p.getProperty(dependencyHandlingPropertyPath)
}


fun createVersionTask(project: Project, name: String) {
    val configure = project.extensions.create("$name", DefaultVersionsPluginExtension::class.java)
    project.task("installVersions") { task: Task ->
        task.doLast {
            val versionPluginConfiguration =
                VersionPluginConfiguration(
                    configure.versionPluginBuilder.getProjectDir(project),
                    configure.versionPluginBuilder,
                    project
                )
            val reset = versionPluginConfiguration.resetMyVersion()
            project.logger.quiet("version reset: $reset")
            val resetDependencyHandling =
                versionPluginConfiguration.dependencyHandlingData.resetDependencyHandlingVersion()
            project.logger.quiet("reset dependencyHandling: $resetDependencyHandling")
            versionPluginConfiguration.publishDependencyHandling()
            project.logger.quiet("published ${versionPluginConfiguration.projectDir} with version: ${versionPluginConfiguration.dependencyHandlingData.nextVersion}")
        }
    }
}

open class DefaultVersionsPluginExtension {
    lateinit var versionPluginBuilder: VersionPluginExtension<*>

    fun setDepending(
        tasks: TaskContainer,
        artifactoryPublishName: String = "artifactoryPublish",
        installVersions: String = "installVersions"
    ) {
        val artifactoryPublish = tasks.getByName(artifactoryPublishName)
        artifactoryPublish.dependsOn(installVersions)
    }
}

class VersionsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        createVersionTask(target, "versionPlugin")
    }
}
