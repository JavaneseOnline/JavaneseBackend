@file:Suppress("NOTHING_TO_INLINE")
package online.javanese.template

import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.a
import kotlinx.html.title

fun FlowOrInteractiveOrPhrasingContent.a(href: String, titleAndText: String) =
        a(href = href) aTag@ {
            this@aTag.title = titleAndText
            +titleAndText
        }
