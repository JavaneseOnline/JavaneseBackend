package online.javanese

import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.post

fun Route.get(path: String, handler: suspend (ApplicationCall) -> Unit) =
        get(path) { handler(call) }

fun Route.get1(handler: suspend (ApplicationCall, String) -> Unit) =
        get("/{a}/") { handler(call, call.parameters["a"]!!) }

fun Route.get2(handler: suspend (ApplicationCall, String, String) -> Unit) =
        get("/{a}/{b}/") { handler(call, call.parameters["a"]!!, call.parameters["b"]!!) }

fun Route.get3(handler: suspend (ApplicationCall, String, String, String) -> Unit) =
        get("/{a}/{b}/{c}/") { handler(call, call.parameters["a"]!!, call.parameters["b"]!!, call.parameters["c"]!!) }

fun Route.post(path: String, handler: suspend (ApplicationCall) -> Unit) =
        post(path) { handler(call) }
