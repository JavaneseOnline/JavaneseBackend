package online.javanese.link

import io.ktor.application.ApplicationCall
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
interface Address</* nope. we need inout */ T, HANDLER/* : suspend Function<Unit>*/> {
    fun url(obj: T): String
    suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: HANDLER): Boolean
}

/**
 * Represents an address of index (for [T] with empty [urlSegment]) or to a dir (otherwise).
 */
class IndexOrSingleSegmDirAddress<T : Any>(
        private val urlSegment: (T) -> String,
        private val bySegment: (String?) -> T?
) : Address<T, suspend ApplicationCall.(T) -> Unit> {
    private val sng = SingleSegmentDirAddress(urlSegment, bySegment)
    override fun url(obj: T): String =
            if (urlSegment(obj).isEmpty()) IndexAddress.url(obj) else sng.url(obj)

    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.(T) -> Unit): Boolean = when (urlParts.size) {
        0 -> bySegment(null).letOrFalse(call, handler)
        1 -> bySegment(urlParts[0]).letOrFalse(call, handler)
        else -> false
    }
}


// /


/**
 * Represents an address to index page (`/`) of current domain.
 */
object IndexAddress : Address<Any?, suspend ApplicationCall.() -> Unit> {
    override fun url(obj: Any?): String = "/"
    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.() -> Unit): Boolean =
            urlParts.isEmpty() && handler(call).let { true }
}

/**
 * Represents an address of a file located in a document root (`/someFile`).
 */
class SingleSegmentFileAddress<T : Any>(
        private val urlSegment: (T) -> String,
        private val bySegment: (String) -> T?
) : Address<T, suspend ApplicationCall.(T) -> Unit> {
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}"
    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.(T) -> Unit): Boolean =
            urlParts.size == 1 && bySegment(urlParts[0]).letOrFalse(call, handler)
}


// /1/


/**
 * Represents an address of a directory located in a document root (`/someDir/`).
 */
class SingleSegmentDirAddress<T : Any>(
        private val urlSegment: (T) -> String,
        private val bySegment: (String) -> T?
) : Address<T, suspend ApplicationCall.(T) -> Unit> {
    override fun url(obj: T): String = "/${urlSegment(obj).encodeForUrl()}/"
    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.(T) -> Unit): Boolean =
            urlParts.size == 1 && bySegment(urlParts[0]).letOrFalse(call, handler)
}

/**
 * Represents an address of a file located in a dir (`/someDir/someFile`).
 */
class TwoSegmentFileAddress<T : Any>(
        private val dirUrlSegment: (T) -> String,
        private val fileUrlSegment: (T) -> String,
        private val bySegments: (dir: String, file: String) -> T?
) : Address<T, suspend ApplicationCall.(T) -> Unit> {
    override fun url(obj: T): String = "/${dirUrlSegment(obj).encodeForUrl()}/${fileUrlSegment(obj).encodeForUrl()}"
    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.(T) -> Unit): Boolean =
            urlParts.size == 2 && bySegments(urlParts[0], urlParts[1]).letOrFalse(call, handler)
}


// /1/2/


/**
 * Represents a two-segment directory path (`/segm1/segm2/`).
 */
class TwoSegmentDirAddress<T : Any>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String,
        private val bySegments: (first: String, second: String) -> T?
) : Address<T, suspend ApplicationCall.(T) -> Unit> {
    override fun url(obj: T): String = "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/"
    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.(T) -> Unit): Boolean =
            urlParts.size == 2 && bySegments(urlParts[0], urlParts[1]).letOrFalse(call, handler)
}


// /1/2/3/


/**
 * Represents a three-segment directory path (`/segm1/segm2/segm3/`).
 */
class ThreeSegmentDirAddress<T : Any>(
        private val firstSegm: (T) -> String,
        private val secondSegm: (T) -> String,
        private val thirdSegm: (T) -> String,
        private val bySegments: (first: String, second: String, third: String) -> T?
) : Address<T, suspend ApplicationCall.(T) -> Unit> {
    override fun url(obj: T): String =
            "/${firstSegm(obj).encodeForUrl()}/${secondSegm(obj).encodeForUrl()}/${thirdSegm(obj).encodeForUrl()}/"
    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.(T) -> Unit): Boolean =
            urlParts.size == 3 && bySegments(urlParts[0], urlParts[1], urlParts[2]).letOrFalse(call, handler)
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
        private val thirdSegm: (C) -> String,
        private val first: (String) -> A?,
        private val second: (A, String) -> B?,
        private val third: (A, B, String) -> C?
) : Address<IN, suspend ApplicationCall.(A, B, C) -> Unit> {
    override fun url(obj: IN): String {
        val c = in2c(obj)
        val b = c2b(c)
        val a = b2a(b)
        return "/${firstSegm(a).encodeForUrl()}/${secondSegm(b).encodeForUrl()}/${thirdSegm(c).encodeForUrl()}/"
    }

    override suspend fun handle(call: ApplicationCall, urlParts: List<String>, handler: suspend ApplicationCall.(A, B, C) -> Unit): Boolean {
        if (urlParts.size != 3)
            return false

        val f = first(urlParts[0]) ?: return false
        val s = second(f, urlParts[1]) ?: return false
        val t = third(f, s, urlParts[2]) ?: return false

        handler(call, f, s, t)
        return true
    }
}


fun String.encodeForUrl(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
fun String.decodeFromUrl(): String = URLDecoder.decode(this, "UTF-8")

private suspend /*inline*/ fun <T : Any> T?.letOrFalse(call: ApplicationCall, func: suspend ApplicationCall.(T) -> Unit) =
        if (this == null) false else { func(call, this); true }
