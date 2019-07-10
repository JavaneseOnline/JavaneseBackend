@file:Suppress("NOTHING_TO_INLINE")
package online.javanese.page

import kotlinx.html.*


inline fun FlowContent.contentCardMain(noinline block: MAIN.() -> Unit) =
        main("content card mdl-shadow--8dp", block)


// tabs

inline fun FlowContent.tabs(noinline block: DIV.() -> Unit) =
        div("no-pad mdl-tabs mdl-js-tabs mdl-js-ripple-effect content-padding-b", block)

inline fun FlowContent.cardWithTabs(noinline block: DIV.() -> Unit) =
        div("content card mdl-shadow--8dp mdl-tabs mdl-js-tabs mdl-js-ripple-effect container-margin-t", block)

inline fun FlowContent.card(moreClasses: String? = null, noinline block: DIV.() -> Unit) =
        div(classes("content card mdl-shadow--8dp container-margin-t", moreClasses), block)

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

inline fun FlowContent.materialDialogBodyUl(noinline block: UL.() -> Unit) =
        ul("mdl-dialog__content", block)

inline fun FlowContent.materialDialogActions(noinline block: DIV.() -> Unit) =
        div("mdl-dialog__actions", block)

// form components

fun FlowContent.materialTextArea(
        areaId: String?, areaName: String, labelBlock: LABEL.() -> Unit,
        areaVId: String? = null, areaVModel: String? = null, areaOnInput: String? = null,
        strangePlace: DIV.() -> Unit = { }
) {
    div(classes = "mdl-textfield mdl-js-textfield mdl-textfield--floating-label") {
        textArea(classes = "mdl-textfield__input") {
            if (areaId != null) id = areaId else if (areaVId != null) attributes["v-bind:id"] = areaVId
            if (areaVModel != null) attributes["v-model"] = areaVModel
            if (areaOnInput != null) attributes["v-on:input"] = areaOnInput
            name = areaName
        }

        label(classes = "mdl-textfield__label") {
            if (areaId != null) htmlFor = areaId else if (areaVId != null) attributes["v-bind:for"] = areaVId

            labelBlock()
        }

        strangePlace()
    }
}

fun FlowContent.radio(
        name: String? = null, value: String? = null,
        beginLabel: LABEL.() -> Unit = {}, input: INPUT.() -> Unit = {}, label: LABEL.() -> Unit
) = label(classes = "mdl-radio mdl-js-radio") {
    beginLabel()

    input(type = InputType.radio, name = name, classes = "mdl-radio__button") {
        input()
        value?.let { this.value = it }
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

fun FlowOrInteractiveOrPhrasingContent
        .materialIconButton(type: ButtonType, moreClasses: String? = null, icon: String, block: BUTTON.() -> Unit) =
        button(type = type,
                classes = classes("mdl-button mdl-js-button mdl-button--icon mdl-js-ripple-effect", moreClasses)) {
            block()

            i(classes = "material-icons") {
                +icon
            }
        }

fun FlowContent.materialInput(
        inputId: String, inputName: String,
        inputBlock: INPUT.() -> Unit, labelBlock: LABEL.() -> Unit, strangePlace: DIV.() -> Unit,
        vModel: String? = null
) {
    div(classes="mdl-textfield mdl-js-textfield mdl-textfield--floating-label") {
        input(type = InputType.text, name = inputName, classes = "mdl-textfield__input") {
            id = inputId
            if (vModel != null) attributes["v-model"] = vModel
            inputBlock()
        }

        label(classes = "mdl-textfield__label") {
            htmlFor = inputId
            labelBlock()
        }

        strangePlace()
    }
}

fun FlowContent.materialCheckBox(
        inputId: String, inputVModel: String? = null, labelBlock: SPAN.() -> Unit
) {
    label(classes = "mdl-checkbox mdl-js-checkbox mdl-js-ripple-effect") {
        htmlFor = inputId

        input(type = InputType.checkBox, classes = "mdl-checkbox__input") {
            id = inputId
            if (inputVModel != null) attributes["v-model"] = inputVModel
        }

        span(classes = "mdl-checkbox__label", block = labelBlock)
    }
}




inline fun classes(basic: String, additional: String?) =
        if (additional == null) basic else basic + ' ' + additional
