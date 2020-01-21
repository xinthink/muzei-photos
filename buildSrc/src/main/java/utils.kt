import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.FileInputStream
import java.util.Properties

operator fun Project.contains(propertyName: String): Boolean = hasProperty(propertyName)

fun Project.loadProperties(path: String, extra: ExtraPropertiesExtension = this.extra)
    = loadProperties(file(path), extra)

fun Project.loadProperties(file: File, extra: ExtraPropertiesExtension = this.extra) {
    if (!file.exists()) return
    Properties().apply {
        FileInputStream(file).use {
            load(it)
            forEach { (k, v) ->
                extra["$k"] = v
            }
        }
    }
}
