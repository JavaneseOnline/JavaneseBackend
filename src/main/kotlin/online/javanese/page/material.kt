@file:Suppress("NOTHING_TO_INLINE")
package online.javanese.page

import kotlinx.html.*
import online.javanese.extensions.MENU
import online.javanese.extensions.menu


inline fun FlowContent.contentCardDiv(noinline block: DIV.() -> Unit) =
        div("content card mdl-shadow--8dp", block)

inline fun FlowContent.contentCardMain(noinline block: MAIN.() -> Unit) =
        main("content card mdl-shadow--8dp", block)


// tabs

inline fun FlowContent.tabs(noinline block: DIV.() -> Unit) =
        div("no-pad mdl-tabs mdl-js-tabs mdl-js-ripple-effect content-padding-b", block)

inline fun FlowContent.cardWithTabs(noinline block: DIV.() -> Unit) =
        div("content card mdl-shadow--8dp mdl-tabs mdl-js-tabs mdl-js-ripple-effect container-margin-t", block)

inline fun DIV.tabBar(noinline block: MENU.() -> Unit) =
        menu("mdl-tabs__tab-bar mdl-color-text--grey-600", block)

inline fun MENU.tabLink(id: String, text: String, active: Boolean = false) =
        a(href = "#$id", classes = if (active) "mdl-tabs__tab is-active" else "mdl-tabs__tab") {
            +text
        }

inline fun DIV.tabPanelNav(id: String, active: Boolean = false, moreClasses: String? = null, noinline block: NAV.() -> Unit) =
        nav(classes = classes(if (active) "mdl-tabs__panel is-active" else "mdl-tabs__panel", moreClasses)) {
            this@nav.id = id

            block()
        }

inline fun DIV.tabPanelSection(id: String, active: Boolean = false, moreClasses: String? = null, noinline block: SECTION.() -> Unit) =
        section (classes = classes(if (active) "mdl-tabs__panel is-active" else "mdl-tabs__panel", moreClasses)) {
            this@section.id = id

            block()
        }


// dialog

inline fun FlowContent.materialDialog(id: String, noinline block: DIALOG.() -> Unit) =
        dialog(classes = "mdl-dialog") {
            this@dialog.id = id
            block()
        }

inline fun DIALOG.materialDialogTitle(noinline block: H4.() -> Unit) =
        h4("mdl-dialog__title", block)

inline fun FlowContent.materialDialogBody(noinline block: DIV.() -> Unit) =
        div("mdl-dialog__content", block)

inline fun FlowContent.materialDialogActions(noinline block: DIV.() -> Unit) =
        div("mdl-dialog__actions", block)

// form components

fun FlowContent.radio(name: String, value: String, label: LABEL.() -> Unit) =
        label(classes = "mdl-radio mdl-js-radio") {
            input(type = InputType.radio, name = name, classes = "mdl-radio__button") {
                this.value = value
            }
            label()
        }

inline fun FlowOrInteractiveOrPhrasingContent
        .materialButton(type: ButtonType, moreClasses: String? = null, noinline block: BUTTON.() -> Unit) =
        button(
                type = type,
                classes = classes("mdl-button mdl-js-button mdl-js-ripple-effect", moreClasses),
                block = block
        )

inline fun FlowOrInteractiveOrPhrasingContent
        .colouredRaisedMaterialButton(type: ButtonType, moreClasses: String? = null, noinline block: BUTTON.() -> Unit) =
        button(
                type = type,
                classes = classes("mdl-button mdl-button--raised mdl-button--colored mdl-js-button mdl-js-ripple-effect", moreClasses),
                block = block
        )

inline fun FlowOrInteractiveOrPhrasingContent
        .raisedMaterialButton(type: ButtonType, moreClasses: String? = null, noinline block: BUTTON.() -> Unit) =
        button(
                type = type,
                classes = classes("mdl-button mdl-button--raised mdl-js-button mdl-js-ripple-effect", moreClasses),
                block = block
        )




inline fun classes(basic: String, additional: String?) =
        if (additional == null) basic else basic + ' ' + additional
