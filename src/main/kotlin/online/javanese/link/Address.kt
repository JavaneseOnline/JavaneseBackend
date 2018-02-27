package online.javanese.link

import io.ktor.http.HttpMethod
import java.net.URLDecoder
import java.net.URLEncoder

// todo: move to KRUD
// todo: make crossinline
// todo: make full

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
 * Represents an address of a file located in a document root (`/someFile`).
 */
class SingleSegmentFileAddress<T>(
        private val urlSegment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}"
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
 * Represents an address of a file located in a dir (`/someDir/someFile`).
 */
class TwoSegmentFileAddress<T>(
        private val dirUrlSegment: (T) -> String,
        private val fileUrlSegment: (T) -> String
) : Address<T> {
    override fun url(obj: T): String = "/${dirUrlSegment(obj).encodeForUrl()}/${fileUrlSegment(obj).encodeForUrl()}"
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
class HierarchicalThreeSegmentDirAddress<A, B, C, IN>(
        private val in2c: (IN) -> C,
        private val c2b: (C) -> B,
        private val b2a: (B) -> A,
        private val firstSegm: (A) -> String,
        private val secondSegm: (B) -> String,
        private val thirdSegm: (C) -> String
) : Address<IN> {
    override fun url(obj: IN): String {
        val c = in2c(obj)
        val b = c2b(c)
        val a = b2a(b)
        return "/${firstSegm(a).encodeForUrl()}/${secondSegm(b).encodeForUrl()}/${thirdSegm(c).encodeForUrl()}/"
    }
}


fun String.encodeForUrl(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
fun String.decodeFromUrl(): String = URLDecoder.decode(this, "UTF-8")
