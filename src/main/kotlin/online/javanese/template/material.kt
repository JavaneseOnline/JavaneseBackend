@file:Suppress("NOTHING_TO_INLINE")
package online.javanese.template

import kotlinx.html.*
import online.javanese.extensions.MENU
import online.javanese.extensions.menu


inline fun FlowContent.contentCardDiv(noinline block: DIV.() -> Unit) =
        div("content card mdl-shadow--8dp", block)

// tabs

inline fun FlowContent.tabs(noinline block: DIV.() -> Unit) =
        div("no-pad mdl-tabs mdl-js-tabs mdl-js-ripple-effect content-padding-b", block)

inline fun DIV.tabBar(noinline block: MENU.() -> Unit) =
        menu("mdl-tabs__tab-bar mdl-color-text--grey-600", block)

inline fun MENU.tabLink(id: String, text: String, active: Boolean = false) =
        a(href = "#$id", classes = if (active) "mdl-tabs__tab is-active" else "mdl-tabs__tab") {
            +text
        }

inline fun DIV.tabPanel(id: String, active: Boolean = false, noinline block: NAV.() -> Unit) =
        nav(classes = if (active) "mdl-tabs__panel is-active" else "mdl-tabs__panel") {
            this@nav.id = id

            block()
        }
