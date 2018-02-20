package online.javanese.link

import kotlinx.html.FlowContent


interface HtmlBlock {
    fun render(where: FlowContent, classes: String? = null)
}

object NoHtmlBlock : HtmlBlock {
    override fun render(where: FlowContent, classes: String?) = Unit
}


interface HtmlBlock1<in T> {
    fun render(where: FlowContent, param: T, classes: String? = null)
}


interface HtmlBlock2<in T, in U> {
    fun render(where: FlowContent, param1: T, param2: U, classes: String? = null)
}


interface HtmlBlock3<in T, in U, in V> {
    fun render(where: FlowContent, param1: T, param2: U, param3: V, classes: String? = null)
}


interface HtmlBlock4<in T, in U, in V, in W> {
    fun render(where: FlowContent, param1: T, param2: U, param3: V, param4: W, classes: String? = null)
}
