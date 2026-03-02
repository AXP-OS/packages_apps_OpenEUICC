package im.angry.openeuicc.build

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

class MyVersioningPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("com.android.application") {
            project.afterEvaluate {

                val android = project.extensions.getByType(BaseExtension::class.java)

                val versionName = resolveVersionName(project)
                val versionCode = resolveVersionCode(project)

                android.defaultConfig.versionName = versionName
                android.defaultConfig.versionCode = versionCode
            }
        }
    }

    private fun resolveVersionName(project: Project): String {
        val prop = project.rootProject.findProperty("versionName")?.toString()
        if (!prop.isNullOrBlank()) return prop
        return getGitVersionName(project)
    }

    private fun resolveVersionCode(project: Project): Int {
        val prop = project.rootProject.findProperty("versionCode")?.toString()
        if (!prop.isNullOrBlank()) return prop.toInt()
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