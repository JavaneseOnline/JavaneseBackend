package online.javanese.page

import kotlinx.html.*


val VkInitWidgetsScriptLine = """VK.init({ apiId: 5748800, onlyWidgets: true });"""

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

fun FlowContent.vkInitForWidgets() = script {
    unsafe {
        +"""VK.init({ apiId: 5748800, onlyWidgets: true });"""
    }
}
