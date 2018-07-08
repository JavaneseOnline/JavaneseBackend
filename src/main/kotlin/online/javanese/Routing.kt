package online.javanese

import io.ktor.application.ApplicationCall
import io.ktor.request.uri
import online.javanese.exception.NotFoundException
import online.javanese.link.Action
import online.javanese.link.Link

// todo: move to KRUD
/**
 * A builder for type-safe routes.
 */
class Routing private constructor(
        private val partsParamName: String,
        private val getRoutes: Array<GetRoute<*, *>>,
        private val postRoutes: Array<PostRoute<*, *>>
) {

    class Builder {
        private val getRoutes = ArrayList<GetRoute<*, *>>()
        private val postRoutes = ArrayList<PostRoute<*, *>>()

        infix fun <T, HANDLER> Link<T, HANDLER>.x(handler: HANDLER) {
            getRoutes.add(GetRoute(this, handler))
        }

        infix fun <T, HANDLER> Action<T, HANDLER>.x(handler: HANDLER) {
            postRoutes.add(PostRoute(this, handler))
        }

        fun build(partsParamName: String) =
                Routing(partsParamName, getRoutes.toTypedArray(), postRoutes.toTypedArray())
    }



    suspend fun get(call: ApplicationCall) {
        val parts = call.parameters.getAll(partsParamName)!!
        if (!getRoutes.any { it.handle(call, parts) }) {
            throw NotFoundException("No handler found for GET-request on URI ${call.request.uri}, URL parts are $parts")
        }
    }

    suspend fun post(call: ApplicationCall) {
        val parts = call.parameters.getAll(partsParamName)!!
        if (!postRoutes.any { it.handle(call, parts) }) {
            throw NotFoundException("No handler found for GET-request on URI ${call.request.uri}, URL parts are $parts")
        }
    }

}

inline fun Routing(
        partsParamName: String,
        init: Routing.Builder.() -> Unit
) = Routing.Builder().apply(init).build(partsParamName)

private class GetRoute<T, HANDLER>(
        private val link: Link<T, HANDLER>,
        private val handler: HANDLER
) {
    suspend fun handle(call: ApplicationCall, urlParts: List<String>) =
            link.handleGet(call, urlParts, handler)
}

private class PostRoute<T, HANDLER>(
        private val action: Action<T, HANDLER>,
        private val handler: HANDLER
) {
    suspend fun handle(call: ApplicationCall, urlParts: List<String>) =
            action.handlePost(call, urlParts, handler)
}
