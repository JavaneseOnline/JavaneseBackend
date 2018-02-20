package online.javanese.link

import io.ktor.http.HttpMethod
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Represents an address.
 * @see Link for [HttpMethod.Get]
 * @see Action for [HttpMethod.Post]
 */
interface Address</* nope. we need inout */ T> {
    fun url(obj: T): String
}

// /

/**
 * Represents an address to index page (`/`) of current domain.
 */
object IndexAddress : Address<Any?> {
    override fun url(obj: Any?): String = "/"
}

/**
 * Represents an address to a part of index page (`/#fragment`) of current domain.
 */
class IndexAddressWithFragment<T>(
        private val fragment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${fragment(obj).encodeForUrl()}"
}

/**
 * Represents an address of a file located in a document root (`/someFile`).
 */
class SingleSegmentFileAddress<T>(
        private val urlSegment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}"
}

/**
 * Represents an address of a part of a file located in document root (`/someFile#fragment`).
 */
class SingleSegmentFileAddressWithFragment<T>(
        private val urlSegment: (T) -> String,
        private val fragment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}#${fragment(obj).encodeForUrl()}"
}

// /1/

/**
 * Represents an address of a directory located in a document root (`/someDir/`).
 */
class SingleSegmentDirAddress<T>(
        private val urlSegment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}/"
}

/**
 * Represents an address of a part of directory located in a document root (`/someDir/#fragment`).
 */
class SingleSegmentDirAddressWithFragment<T>(
        private val urlSegment: (T) -> String,
        private val fragment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}/#${fragment(obj).encodeForUrl()}"
}

/**
 * Represents an address of a file located in a dir (`/someDir/someFile`).
 */
class TwoSegmentFileAddress<T>(
        private val dirUrlSegment: (T) -> String,
        private val fileUrlSegment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${dirUrlSegment(obj).encodeForUrl()}/${fileUrlSegment(obj).encodeForUrl()}"
}

/**
 * Represents an address of a file located in a dir (`/someDir/someFile#fragment`).
 */
class TwoSegmentFileAddressWithFragment<T>(
        private val dirUrlSegment: (T) -> String,
        private val fileUrlSegment: (T) -> String,
        private val fragment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${dirUrlSegment(obj).encodeForUrl()}/${fileUrlSegment(obj).encodeForUrl()}#${fragment(obj).encodeForUrl()}"
}


// /1/2/


/**
 * Represents a two-segment directory path (`/segm1/segm2/`).
 */
class TwoSegmentDirAddress<T>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/"
}


// /1/2/3/

/**
 * Represents a three-segment directory path (`/segm1/segm2/segm3/`).
 */
class ThreeSegmentDirAddress<T>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String,
        private val thirdSegm: (T) -> String
) : Address<T> {
    override fun url(obj: T): String =
            "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/${thirdSegm(obj).encodeForUrl()}/"
}

/**
 * Acts like [ThreeSegmentDirAddress] but helps in fetching parent objects.
 */
class HierarchicalThreeSegmentDirAddress<A, B, C>(
        private val c2b: (C) -> B,
        private val b2a: (B) -> A,
        private val firstSegm: (A) -> String,
        private val secondSegm: (B) -> String,
        private val thirdSegm: (C) -> String
) : Address<C> {
    override fun url(obj: C): String {
        val s = c2b(obj)
        val f = b2a(s)
        return "/${firstSegm(f).encodeForUrl()}/${secondSegm(s).encodeForUrl()}/${thirdSegm(obj).encodeForUrl()}/"
    }
}

/**
 * Represents a three-segment directory path with a fragment (`/segm1/segm2/segm3/#fragment`).
 */
class ThreeSegmentDirAddressWithFragment<T>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String,
        private val thirdSegm: (T) -> String,
        private val fragment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String =
            "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/${thirdSegm(obj).encodeForUrl()}/#${fragment(obj).encodeForUrl()}"
}

/**
 * Acts like [ThreeSegmentDirAddressWithFragment] but helps in fetching parent objects.
 */
class HierarchicalThreeSegmentDirAddressWithFragment<A, B, C, D>(
        private val d2c: (D) -> C,
        private val c2b: (C) -> B,
        private val b2a: (B) -> A,
        private val firstSegm: (A) -> String,
        private val secondSegm: (B) -> String,
        private val thirdSegm: (C) -> String,
        private val fragment: (D) -> String
) : Address<D> {
    override fun url(obj: D): String {
        val c = d2c(obj)
        val b = c2b(c)
        val a = b2a(b)
        return "/${firstSegm(a).encodeForUrl()}/${secondSegm(b).encodeForUrl()}/${thirdSegm(c).encodeForUrl()}/#${fragment(obj).encodeForUrl()}"
    }
}



fun String.encodeForUrl(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
fun String.decodeFromUrl(): String = URLDecoder.decode(this, "UTF-8")
