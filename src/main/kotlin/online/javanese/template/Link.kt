package online.javanese.template

import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import online.javanese.extensions.encodeForUrl


// todo: shouldn't I move this class to a library?

/**
 * Represents a web link to an object.
 */
interface Link<T> {
    fun insert(doc: FlowOrInteractiveOrPhrasingContent, obj: T)
}

/**
 * Represents a link to index page (`/`) of current domain.
 */
class IndexLink<T>(
        private val linkText: (T) -> String
) : Link<T> {
    override fun insert(doc: FlowOrInteractiveOrPhrasingContent, obj: T) =
            doc.a(href = "/", titleAndText = linkText(obj))
}

/**
 * Represents a link to a directory located in a document root (`/someDir/`).
 */
class SingleSegmentDirLink<T>(
        private val urlSegment: (T) -> String,
        private val linkText: (T) -> String
) : Link<T> {
    override fun insert(doc: FlowOrInteractiveOrPhrasingContent, obj: T) =
            doc.a(href = "/${urlSegment(obj).encodeForUrl()}/", titleAndText = linkText(obj))
}

/**
 * Represents a link to index (for [T] with empty [urlSegment]) or to a dir (otherwise).
 */
class IndexOrSingleSegmDirLink<T>(
        private val urlSegment: (T) -> String,
        private val linkText: (T) -> String
) : Link<T> {
    private val idx = IndexLink(linkText)
    private val sng = SingleSegmentDirLink(urlSegment, linkText)
    override fun insert(doc: FlowOrInteractiveOrPhrasingContent, obj: T) =
            if (urlSegment(obj).isEmpty()) idx.insert(doc, obj) else sng.insert(doc, obj)
}

/**
 * Represents a two-segment directory path (`/segm1/segm2/`).
 */
class TwoSegmentDirLink<F, S>(
        private val fUrlSegment: (F) -> String,
        private val sUrlSegment: (S) -> String,
        private val linkText: (F, S) -> String
) : Link<Pair<F, S>> {
    override fun insert(doc: FlowOrInteractiveOrPhrasingContent, obj: Pair<F, S>) =
            doc.a(
                    href = "/${fUrlSegment(obj.first).encodeForUrl()}/${sUrlSegment(obj.second).encodeForUrl()}/",
                    titleAndText = linkText(obj.first, obj.second)
            )
}

/**
 * Represents a three-segment directory path (`/segm1/segm2/segm3/`).
 */
class ThreeSegmentDirLink<F, S, T>(
        private val fUrlSegment: (F) -> String,
        private val sUrlSegment: (S) -> String,
        private val tUrlSegment: (T) -> String,
        private val linkText: (F, S, T) -> String
) : Link<Triple<F, S, T>> {
    override fun insert(doc: FlowOrInteractiveOrPhrasingContent, obj: Triple<F, S, T>) =
            doc.a(
                    href = "/${fUrlSegment(obj.first).encodeForUrl()}/${sUrlSegment(obj.second).encodeForUrl()}/${tUrlSegment(obj.third).encodeForUrl()}/",
                    titleAndText = linkText(obj.first, obj.second, obj.third)
            )
}
