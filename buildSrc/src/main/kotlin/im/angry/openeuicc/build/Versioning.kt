package im.angry.openeuicc.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

class MyVersioningPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            val versionName = resolveVersionName(project)
            val versionCode = resolveVersionCode(project)

            project.extensions.extraProperties["versionName"] = versionName
            project.extensions.extraProperties["versionCode"] = versionCode
        }
    }

    private fun resolveVersionName(project: Project): String {
        val prop = project.rootProject.findProperty("versionName")?.toString()
        if (!prop.isNullOrBlank()) {
            return prop
        }

        // fallback to existing git-based logic
        return getGitVersionName(project)
    }

    private fun resolveVersionCode(project: Project): Int {
        val prop = project.rootProject.findProperty("versionCode")?.toString()
        if (!prop.isNullOrBlank()) {
            return prop.toInt()
        }

        // fallback to existing git-based logic
        return getGitVersionCode(project)
    }

    private fun getGitVersionName(project: Project): String {
        val stdout = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "describe", "--tags", "--dirty", "--always")
            standardOutput = stdout
        }
        return stdout.toString().trim()
    }

    private fun getGitVersionCode(project: Project): Int {
        val stdout = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        return stdout.toString().trim().toInt()
    }
}