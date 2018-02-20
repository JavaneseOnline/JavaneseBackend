package online.javanese.link

import kotlinx.html.*

/**
 * Represents form action.
 */
class Action<T>(
        private val address: Address<T>
) {

    fun renderForm(flow: FlowContent, obj: T, encType: FormEncType? = null, classes : String? = null, block : FORM.() -> Unit = {}) : Unit =
            flow.form(action = address.url(obj), encType = encType, method = FormMethod.post, classes = classes, block = block)

}
