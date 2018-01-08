package online.javanese.template

import kotlinx.html.FlowContent
import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import kotlinx.html.visit

fun FlowContent.main(classes: String? = null, visitor: MAIN.() -> Unit) =
        MAIN(consumer, classes).visit(visitor)

class MAIN(consumer: TagConsumer<*>, classes: String?) :
        HTMLTag("main", consumer, mapOf("class" to (classes ?: "")), inlineTag = false, emptyTag = false),
        FlowContent
