package online.javanese.link


fun String.withFragment(fragment: String): String {
    val stripped = if (fragment.startsWith("#")) fragment.substring(1) else fragment

    return if (stripped.isEmpty()) this else {
        val idxOfHash = indexOf('#')
        val encoded = stripped.encodeForUrl()
        if (idxOfHash < 0) {
            this + '#' + encoded
        } else {
            val withHash = idxOfHash + 1
            buildString(withHash + encoded.length) {
                append(this, 0, withHash)
                append(encoded)
            }
        }
    }
}
