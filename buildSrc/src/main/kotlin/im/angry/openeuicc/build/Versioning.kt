package im.angry.openeuicc.build

import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.ByteArrayOutputStream

val Project.gitVersionCode: Int
    get() =
        try {
            val stdout = ByteArrayOutputStream()
            exec {
                commandLine("git", "rev-list", "--first-parent", "--count", "HEAD")
                standardOutput = stdout
            }
            stdout.toString("utf-8").trim('\n').toInt()
        } catch (_: Exception) {
            0
        }

val Project.gitVersionName: String
    get() =
        try {
            val stdout = ByteArrayOutputStream()
            exec {
                commandLine("git", "describe", "--always", "--tags", "--dirty")
                standardOutput = stdout
            }
            stdout.toString("utf-8").trim('\n')
        } catch (_: Exception) {
            "Unknown"
        }

class MyVersioningPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configure<BaseAppModuleExtension> {
            defaultConfig {
                versionCode = try {
                    val versionOverride = System.getenv("VERSION_CODE")
                    if (versionOverride != null) versionOverride.toInt() else gitVersionCode
                } catch (e: Exception) {
                    0
                }
                versionName = try {
                    val versionOverride = System.getenv("VERSION_NAME")
                    if (versionOverride != null) versionOverride.removePrefix("unpriv-") else gitVersionName
                } catch (e: Exception) {
                    "Unknown"
                }
            }

            applicationVariants.all {
                if (name == "debug") {
                    outputs.forEach {
                        (it as ApkVariantOutputImpl).versionCodeOverride =
                            (System.currentTimeMillis() / 1000).toInt()
                    }
                }
            }
        }
    }
}