package online.javanese.page

import kotlinx.html.*
import online.javanese.model.VkPostInfo


fun FlowOrPhrasingOrMetaDataContent.vkOpenApiScript() = script(src = "https://vk.com/js/api/openapi.js?136") {
    attributes["id"] = "vk_openapi_js"
}

fun FlowOrPhrasingOrMetaDataContent.vkShareScript() = script(src = "https://vk.com/js/api/share.js?94") {
    charset = "windows-1251"
}


/// VK comments ///

val VkInitWidgetsScriptLine = """VK.init({ apiId: 5748800, onlyWidgets: true });"""

// requires vkOpenApiScript
fun FlowContent.vkComments(pageId: String, init: Boolean, classes: String? = null) {
    div(classes = classes) {
        id = "vk_comments"
    }
    script {
        unsafe {
            if (init)
                +VkInitWidgetsScriptLine

            +"""VK.Widgets.Comments("vk_comments", {}, '$pageId');"""
        }
    }
}


/// VK post embed ///

// requires vkOpenApiScript
fun FlowContent.vkEmbeddedPost(info: VkPostInfo, classes: String? = null) = section(classes) {
    div {
        id = "vk_post_${info.id}"
    }
    script {
        val vkPostId = info.id
        val vkPostHash = info.hash
        val vkPostIdParts = vkPostId.split('_')
        unsafe {
            +"""VK.Widgets.Post('vk_post_$vkPostId', '${vkPostIdParts[0]}', '${vkPostIdParts[1]}', '$vkPostHash');"""
        }
    }
}


/// VK like button ///

// requires vkOpenApiScript
fun FlowContent.vkLikeButton() = div {
    id ="vk_like"
    style = "display: inline-block"
}

fun initVkWidgetJs(id: String) = "VK.Widgets.Like('vk_like', { type: 'button' }, '$id');"


/// VK Share button ///

// requires vkShareScript
fun documentWriteVkShareButton(text: String) =
        "document.write(VK.Share.button(false, { type: 'round', text: '$text' }));"


/// TG post ///

fun FlowContent.tgPost(post: String, classes: String? = null) = div(classes) {
    script(src = "https://telegram.org/js/telegram-widget.js?1") {
        async = true
        attributes["data-telegram-post"] = post
        attributes["data-width"] = "100%"
    }
}


/// VK and TG ///

fun FlowContent.vkAndTgPosts(vkPostInfo: VkPostInfo?, tgPost: String?, initVk: Boolean) {
    if (vkPostInfo != null || !tgPost.isNullOrBlank()) {
        div("mdl-grid") {
            if (vkPostInfo != null) {
                if (initVk) vkOpenApiScript()
                vkEmbeddedPost(vkPostInfo, "mdl-cell mdl-cell--6-col-desktop mdl-cell--4-col")
            }

            if (tgPost != null && tgPost.isNotBlank()) {
                tgPost(tgPost, "mdl-cell mdl-cell--6-col-desktop mdl-cell--4-col")
            }
        }
    }
}
