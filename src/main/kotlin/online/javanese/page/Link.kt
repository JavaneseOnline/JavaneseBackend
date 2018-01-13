package online.javanese.page

import kotlinx.html.A
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.a
import kotlinx.html.title
import java.net.URLDecoder
import java.net.URLEncoder


// todo: shouldn't I move this class to a library?

/**
 * Represents a web link to an object.
 */
interface Link<in T> {
    fun render(doc: FlowOrInteractiveOrPhrasingContent, obj: T) =
            doc.a(href = url(obj), titleAndText = linkText(obj))

    fun renderCustom(doc: FlowOrInteractiveOrPhrasingContent, obj: T, block: A.(text: String) -> Unit) =
            doc.a(href = url(obj), title = linkText(obj), block = block)

    fun linkText(obj: T): String
    fun url(obj: T): String
}

/**
 * Represents a link to index page (`/`) of current domain.
 */
class IndexLink<T>(
        private val linkText: (T) -> String
) : Link<T> {
    override fun linkText(obj: T): String = linkText.invoke(obj)
    override fun url(obj: T): String = "/"
}

/**
 * Represents a link to a directory located in a document root (`/someDir/`).
 */
class SingleSegmentDirLink<T>(
        private val urlSegment: (T) -> String,
        private val linkText: (T) -> String
) : Link<T> {
    override fun linkText(obj: T): String = linkText.invoke(obj)
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}/"
}

/**
 * Represents a link to index (for [T] with empty [urlSegment]) or to a dir (otherwise).
 */
class IndexOrSingleSegmDirLink<T>(
        private val urlSegment: (T) -> String,
        linkText: (T) -> String
) : Link<T> {
    private val idx = IndexLink(linkText)
    private val sng = SingleSegmentDirLink(urlSegment, linkText)
    override fun linkText(obj: T): String =
            if (urlSegment(obj).isEmpty()) idx.linkText(obj) else sng.linkText(obj)
    override fun url(obj: T): String =
            if (urlSegment(obj).isEmpty()) idx.url(obj) else sng.url(obj)
}

/**
 * Represents a two-segment directory path (`/segm1/segm2/`).
 */
class TwoSegmentDirLink<T>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String,
        private val linkText: (T) -> String
) : Link<T> {
    override fun linkText(obj: T): String = linkText.invoke(obj)
    override fun url(obj: T): String = "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/"
}

/**
 * Represents a three-segment directory path (`/segm1/segm2/segm3/`).
 */
class ThreeSegmentDirLink<T>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String,
        private val thirdSegm: (T) -> String,
        private val linkText: (T) -> String
) : Link<T> {
    override fun linkText(obj: T): String = linkText.invoke(obj)
    override fun url(obj: T): String =
            "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/${thirdSegm(obj).encodeForUrl()}/"
}

/**
 * Represents a three-segment directory path with a fragment (`/segm1/segm2/segm3/#fragment`).
 */
class ThreeSegmentDirLinkWithFragment<T>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String,
        private val thirdSegm: (T) -> String,
        private val fragment: (T) -> String,
        private val linkText: (T) -> String
) : Link<T> {
    override fun linkText(obj: T): String = linkText.invoke(obj)
    override fun url(obj: T): String =
            "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/${thirdSegm(obj).encodeForUrl()}/#${fragment(obj).encodeForUrl()}"
}



fun String.encodeForUrl(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
fun String.decodeFromUrl(): String = URLDecoder.decode(this, "UTF-8")

@Suppress("NOTHING_TO_INLINE")
inline fun FlowOrInteractiveOrPhrasingContent.a(href: String, title: String, noinline block: A.(String) -> Unit) =
        a(href = href) aTag@ {
            this@aTag.title = title
            block(title)
        }
