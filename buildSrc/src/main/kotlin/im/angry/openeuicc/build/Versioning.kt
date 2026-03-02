package im.angry.openeuicc.build

import org.gradle.api.Project

object Versioning {

    private fun getRequiredProperty(project: Project, name: String): String {
        val value = project.rootProject.findProperty(name)?.toString()
        require(!value.isNullOrBlank()) {
            "Required property '$name' not found in root gradle.properties"
        }
        return value
    }

    fun getVersionName(project: Project): String {
        return getRequiredProperty(project, "versionName")
    }

    fun getVersionCode(project: Project): Int {
        return getRequiredProperty(project, "versionCode").toInt()
    }

    fun applyVersioning(project: Project) {
        project.extensions.extraProperties.set(
            "versionName",
            getVersionName(project)
        )
        project.extensions.extraProperties.set(
            "versionCode",
            getVersionCode(project)
        )
    }
}