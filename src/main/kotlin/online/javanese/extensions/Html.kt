package online.javanese.extensions

import kotlinx.html.*


@Suppress("NOTHING_TO_INLINE")
inline fun FlowContent.contentDiv(noinline block: DIV.() -> Unit) =
        div(classes = "content card mdl-shadow--8dp", block = block)



fun FlowContent.menu(classes: String? = null, visitor: MENU.() -> Unit) = MENU(consumer, classes).visit(visitor)

class MENU(consumer: TagConsumer<*>, classes: String?) :
        HTMLTag("menu", consumer, mapOf("class" to (classes ?: "")), inlineTag = false, emptyTag = false),
        FlowContent
