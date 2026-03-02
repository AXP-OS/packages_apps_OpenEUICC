package im.angry.openeuicc.build

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

class MyVersioningPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        // early set on android.defaultConfig for Kotlin DSL visibility
        project.plugins.withId("com.android.application") {

            val versionName = resolveVersionName(project)
            val versionCode = resolveVersionCode(project)

            project.extensions.findByType(BaseExtension::class.java)?.apply {
                defaultConfig.versionName = versionName
                defaultConfig.versionCode = versionCode
            }

            // use androidComponents to ensure the variant API sees the correct version
            project.extensions.getByType(
                ApplicationAndroidComponentsExtension::class.java
            ).finalizeDsl { extension ->
                extension.defaultConfig.versionName = versionName
                extension.defaultConfig.versionCode = versionCode
            }
        }
    }

    private fun resolveVersionName(project: Project): String {
        project.rootProject.findProperty("versionName")?.toString()?.let {
            if (it.isNotBlank()) return it
        }
        return getGitVersionName(project)
    }

    private fun resolveVersionCode(project: Project): Int {
        project.rootProject.findProperty("versionCode")?.toString()?.let {
            if (it.isNotBlank()) return it.toInt()
        }
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