package online.javanese

import java.net.URLDecoder
import java.net.URLEncoder


fun String.encodeForUrl(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
fun String.decodeFromUrl(): String = URLDecoder.decode(this, "UTF-8")

