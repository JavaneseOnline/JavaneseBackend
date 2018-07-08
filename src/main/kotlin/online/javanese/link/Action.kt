package online.javanese.link

import io.ktor.application.ApplicationCall
import kotlinx.html.*

/**
 * Represents form action bound to POST-request handler type.
 */
class Action<T, HANDLER/* : suspend Function<Unit>*/>(
        private val address: Address<T, HANDLER>
) {

    fun renderForm(flow: FlowContent, obj: T, encType: FormEncType? = null, classes : String? = null, block : FORM.() -> Unit = {}) : Unit =
            flow.form(action = address.url(obj), encType = encType, method = FormMethod.post, classes = classes, block = block)

    suspend fun handlePost(call: ApplicationCall, urlParts: List<String>, handler: HANDLER): Boolean =
            address.handle(call, urlParts, handler)

}
