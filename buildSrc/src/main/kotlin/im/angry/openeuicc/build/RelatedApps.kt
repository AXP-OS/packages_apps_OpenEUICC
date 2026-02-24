package im.angry.openeuicc.build

import com.android.build.api.dsl.VariantDimension
import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils

/**
 * https://web.dev/get-installed-related-apps/?hl=en
 */
fun VariantDimension.emitAssetStatements(vararg sites: String) {
    val relation = listOf("delegate_permission/common.handle_all_urls")
    val statements = sites.map {
        val target = mapOf("namespace" to "web", "site" to it)
        mapOf("relation" to relation, "target" to target)
    }
    val output = JsonOutput.toJson(statements)
    resValue(type = "string", name = "asset_statements", value = StringEscapeUtils.escapeJava(output))
}
