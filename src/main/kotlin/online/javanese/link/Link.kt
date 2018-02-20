package online.javanese.link

import kotlinx.html.*
import online.javanese.page.a

/**
 * Represents a web link to an object.
 */
class Link<T>(
        private val address: Address<T>,
        private val linkText: (T) -> String
) : HtmlBlock1<T> {

    override fun render(where: FlowContent, param: T, classes: String?) {
        val upCasted: FlowOrInteractiveOrPhrasingContent = where
        render(upCasted, param, classes)
    }

    fun render(doc: FlowOrInteractiveOrPhrasingContent, obj: T, classes: String? = null) =
            doc.a(href = address.url(obj), titleAndText = linkText.invoke(obj), classes = classes)

    fun renderCustom(
            doc: FlowOrInteractiveOrPhrasingContent,
            obj: T,
            classes: String? = null,
            block: A.(text: String) -> Unit = {}
    ) =
            doc.a(href = address.url(obj), title = linkText.invoke(obj), classes = classes, block = block)

    fun linkText(obj: T): String = linkText.invoke(obj)
    fun url(obj: T): String = address.url(obj)
}

@Suppress("NOTHING_TO_INLINE")
inline fun FlowOrInteractiveOrPhrasingContent.a(href: String, title: String, classes: String? = null, noinline block: A.(String) -> Unit) =
        a(href = href, classes = classes) aTag@ {
            this@aTag.title = title
            block(title)
        }
