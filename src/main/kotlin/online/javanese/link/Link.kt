package online.javanese.link

import io.ktor.application.ApplicationCall
import kotlinx.html.*
import online.javanese.Just
import online.javanese.page.a

/**
 * Represents a web link to an object bound to GET-request handler type.
 */
interface Link<T, HANDLER/* : suspend Function<Unit>*/> : HtmlBlock1<T> {
    fun render(doc: FlowOrInteractiveOrPhrasingContent, obj: T, classes: String? = null)
    fun renderCustom(
            doc: FlowOrInteractiveOrPhrasingContent, obj: T, classes: String? = null, block: A.(text: String) -> Unit = {}
    )

    fun linkText(obj: T): String
    fun url(obj: T): String

    suspend fun handleGet(call: ApplicationCall, urlParts: List<String>, handler: HANDLER): Boolean
}

interface LinkWithFragment<T, HANDLER> : Link<T, HANDLER> {
    fun fragment(obj: T): String
}

fun <T, HANDLER> Link(
        address: Address<T, HANDLER>,
        linkText: (T) -> String
): Link<T, HANDLER> =
        UniLink(address, linkText, Just(null))

fun <T, HANDLER> Link(
        address: Address<T, HANDLER>,
        linkText: (T) -> String,
        fragment: (T) -> String
): LinkWithFragment<T, HANDLER> =
        UniLink(address, linkText, fragment)

private class UniLink<T, HANDLER/* : suspend Function<Unit>*/>(
        private val address: Address<T, HANDLER>,
        private val linkText: (T) -> String,
        private val fragment: (T) -> String?
) : LinkWithFragment<T, HANDLER> {

    override fun render(where: FlowContent, param: T, classes: String?) {
        val upCasted: FlowOrInteractiveOrPhrasingContent = where
        render(upCasted, param, classes)
    }

    override fun render(doc: FlowOrInteractiveOrPhrasingContent, obj: T, classes: String?) =
            doc.a(href = url(obj), titleAndText = linkText.invoke(obj), classes = classes)

    override fun renderCustom(doc: FlowOrInteractiveOrPhrasingContent, obj: T, classes: String?, block: A.(text: String) -> Unit) =
            doc.a(href = url(obj), title = linkText.invoke(obj), classes = classes, block = block)

    override fun linkText(obj: T): String = linkText.invoke(obj)
    override fun url(obj: T): String {
        val addr = address.url(obj)
        val fragment = fragment.invoke(obj)
        return fragment?.let { "$addr#$fragment" } ?: addr
    }
    override fun fragment(obj: T): String =
            fragment.invoke(obj)!!

    override suspend fun handleGet(call: ApplicationCall, urlParts: List<String>, handler: HANDLER): Boolean =
            address.handle(call, urlParts, handler)

}

@Suppress("NOTHING_TO_INLINE")
inline fun FlowOrInteractiveOrPhrasingContent.a(href: String, title: String, classes: String? = null, noinline block: A.(String) -> Unit) =
        a(href = href, classes = classes) aTag@ {
            this@aTag.title = title
            block(title)
        }
