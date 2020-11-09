package org.daiv.dependency

import kotlin.reflect.KClass


fun String.incrementVersion(): String {
    val split = split(".")
    val last = (split[split.size - 1]).toInt() + 1
    val drop = split.dropLast(1)
    return drop.joinToString(".") + ".$last"
}

fun <T : Any> T.incrementVersion(func: T.() -> String): String {
    return func().incrementVersion()
}

interface Incrementable<T>{
    fun versionObject():T = this as T
    fun setVersion(member: T.()->String) = incrementVersion { versionObject().member() }
}

class VersionPluginExtension<T : Any> {
    lateinit var clazz: KClass<T>
    lateinit var versionJsonPath: String
    lateinit var resetVersion: T.(String) -> T
    lateinit var versionMember: T.()->String
    var versionKey = "dependencyHandlingVersion"
    var publishTaskName = "artifactoryPublish"
    var cleanTaskName = "clean"
    var dependencyHandlingProperties = "dependencyHandling.properties"
    var dependencyHandlingPropertyPath = "path"
    var versionJsonName = "versions.json"
    var versionJsonResourcePath = "src/main/resources"
    var versionsProperties = "version.properties"
}

class VersionsPluginBuilder<T : Any>(
    path: String,
    clazz: KClass<T>,
    configure: VersionPluginExtension<T>.() -> Unit
) {
    val extension: VersionPluginExtension<T> = VersionPluginExtension()

    init {
        extension.clazz = clazz
        extension.versionJsonPath = path
        extension.configure()
    }
}
