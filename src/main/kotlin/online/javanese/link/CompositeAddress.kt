package online.javanese.link


/**
 * Represents an address of index (for [T] with empty [urlSegment]) or to a dir (otherwise).
 */
class IndexOrSingleSegmDirAddress<T>(
        private val urlSegment: (T) -> String
) : Address<T> {
    private val sng = SingleSegmentDirAddress(urlSegment)
    override fun url(obj: T): String =
            if (urlSegment(obj).isEmpty()) IndexAddress.url(obj) else sng.url(obj)
}
