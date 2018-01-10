package online.javanese.page

import kotlinx.html.FlowContent
import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import kotlinx.html.visit

fun FlowContent.main(classes: String? = null, visitor: MAIN.() -> Unit) =
        MAIN(consumer, classes).visit(visitor)

class MAIN(consumer: TagConsumer<*>, classes: String?) :
        HTMLTag("main", consumer, mapOf("class" to (classes ?: "")), inlineTag = false, emptyTag = false),
        FlowContent


fun FlowContent.menu(classes: String? = null, visitor: MENU.() -> Unit) =
        MENU(consumer, classes).visit(visitor)

class MENU(consumer: TagConsumer<*>, classes: String?) :
        HTMLTag("menu", consumer, mapOf("class" to (classes ?: "")), inlineTag = false, emptyTag = false),
        FlowContent
