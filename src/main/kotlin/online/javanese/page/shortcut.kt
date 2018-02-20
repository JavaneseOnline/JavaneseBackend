@file:Suppress("NOTHING_TO_INLINE")
package online.javanese.page

import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.a
import kotlinx.html.title


inline fun FlowOrInteractiveOrPhrasingContent.a(href: String, titleAndText: String, classes: String? = null) =
        a(href = href, classes = classes) aTag@ {
            this@aTag.title = titleAndText
            +titleAndText
        }
