package online.javanese.social

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.param
import io.ktor.routing.route
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.sessions
import kotlinx.html.FlowContent
import online.javanese.link.encodeForUrl
import online.javanese.page.a


class UserSessions(
        private val sessionName: String,
        val sources: Map<String, OAuthSource>,
        private val siteUrl: String,
        private val authName: String,
        private val oauthPathTemplate: String,
        private val providerUrl: (OAuthServerSettings? /* null for logout */) -> String,
        private val providerName: ApplicationCall.() -> String,
        private val httpClient: HttpClient
) {

    fun configureSession(app: Application) = with (app) {
        install(Sessions) {
            cookie<User>(sessionName, SessionStorageMemory()) {
                cookie.path = "/"
            }
        }
    }

    fun authEndpoint(routing: Routing) = with(routing) {
        authenticate(authName) {
            route(oauthPathTemplate) {
                param("error") {
                    handle {
                        // won't be triggered until https://github.com/ktorio/ktor/issues/378 fixed
                        call.respondText(call.parameters.getAll("error").orEmpty().joinToString())
                    }
                }

                handle {
                    val providerName = call.providerName()

                    val provider = sources[providerName]
                    val principal = call.authentication.principal<OAuthAccessTokenResponse>()
                    if (provider === null) {
                        call.sessions.clear(sessionName)
                    } else if (principal !== null) {
                        call.sessions.set(sessionName, provider.resolveUserFor(principal))
                    }

                    call.respondRedirect(call.parameters["to"]?.takeIf { URLBuilder(it).host == "localhost" } ?: "/")
                }
            }
        }
    }

    fun currentUser(call: ApplicationCall): User? =
            call.sessions.get(sessionName) as User?

    private val oAuthUrlProvider = { call: ApplicationCall, oauth: OAuthServerSettings ->
        URLBuilder(siteUrl)
                .takeFrom(providerUrl(oauth))
                .also { builder -> builder.parameters["to"] = call.parameters["to"] ?: "/" }
                .buildString()
    }

    fun configureAuth(config: Authentication.Configuration) = with (config) {
        oauth(name = authName) {
            // these `val`s are for moving receiver (ApplicationCall) to parameters

            client = httpClient

            val lkp = { call: ApplicationCall ->
                sources[call.providerName()]?.oauth
            }
            providerLookup = lkp

            urlProvider = oAuthUrlProvider
        }
    }

    fun oauthLink(oauth: OAuthServerSettings, to: String): String =
            providerUrl(oauth) + "?to=" + to.encodeForUrl()

    fun logoutLink(to: String): String =
            providerUrl(null) + "?to=" + to.encodeForUrl()

    fun FlowContent.authPrompt(text: String, redirectUri: String) {
        +text
        sources.values.forEach { source ->
            source.oauth?.let { oauth ->
                a(href = oauthLink(oauth, redirectUri), titleAndText = source.name)
            }
        }
        +"."
    }

}
