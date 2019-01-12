package online.javanese

import io.ktor.request.ApplicationRequest


enum class RequestedWith {
    XMLHttpRequest, Unknown
}

fun ApplicationRequest.requestedWith(): RequestedWith =
        if (headers["X-Requested-With"] == "XMLHttpRequest") RequestedWith.XMLHttpRequest
        else RequestedWith.Unknown
