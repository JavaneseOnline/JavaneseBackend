package online.javanese.comments

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.github.andrewoma.kwery.core.Session
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
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Routing
import io.ktor.routing.param
import io.ktor.routing.route
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.sessions
import online.javanese.RequestedWith
import online.javanese.exception.BadRequestException
import online.javanese.exception.ForbiddenException
import online.javanese.exception.NotFoundException
import online.javanese.exception.UnauthorizedException
import online.javanese.krud.kwery.Uuid
import online.javanese.link.Action
import online.javanese.link.encodeForUrl
import online.javanese.requestedWith
import java.time.Instant
import java.util.*


class CommentsSource(
        val name: String,
        val oauth: OAuthServerSettings?,
        val resolveCommenterFor: suspend (OAuthAccessTokenResponse) -> Commenter
)

class Commenter(
        val source: String,
        val id: String,
        val displayName: String,
        val avatarUrl: String,
        val pageUrl: String
)

class Comments(
        session: Session,
        val commentAction: Action<Unit, *>,
        private val httpClient: HttpClient,
        private val authName: String,
        private val sources: Map<String, CommentsSource>,
        private val oauthPathTemplate: String,
        private val providerName: ApplicationCall.() -> String,
        private val siteUrl: String,
        private val providerUrl: (OAuthServerSettings? /* null for logout */) -> String,
        private val commenterSessionName: String,
        vararg supportedTables: CommentsFor<*>
) {

    private val dao = CommentDao(session, CommentTable(sources))
    private val tables = supportedTables.associateBy(CommentsFor<*>::joinTableName)

    fun configureSession(app: Application) = with (app) {
        install(Sessions) {
            cookie<Commenter>(commenterSessionName, SessionStorageMemory()) {
                cookie.path = "/"
            }
        }
    }

    fun configureAuth(config: Authentication.Configuration) = with (config) {
        oauth(name = authName) {
            // these `val`s are for moving receiver (ApplicationCall) to parameters

            client = httpClient

            val lkp = { call: ApplicationCall ->
                sources[call.providerName()]?.oauth
            }
            providerLookup = lkp

            val prov = { call: ApplicationCall, oauth: OAuthServerSettings ->
                URLBuilder(siteUrl)
                        .takeFrom(providerUrl(oauth))
                        .also { builder -> builder.parameters["to"] = call.parameters["to"] ?: "/" }
                        .buildString()
            }
            urlProvider = prov
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
                        call.sessions.clear(commenterSessionName)
                    } else if (principal !== null) {
                        call.sessions.set(commenterSessionName, provider.resolveCommenterFor(principal))
                    }

                    call.respondRedirect(call.parameters["to"]?.takeIf { URLBuilder(it).host == "localhost" } ?: "/")
                }
            }
        }
    }

    fun currentCommenter(call: ApplicationCall): Commenter? =
            call.sessions.get(commenterSessionName) as Commenter?

    fun <E> fetch(join: CommentsFor<E>, entity: E): CommentsTree =
            dao.findTree(join, entity)

    fun oauthLink(oauth: OAuthServerSettings, to: String): String =
            providerUrl(oauth) + "?to=" + to.encodeForUrl()

    fun logoutLink(to: String): String =
            providerUrl(null) + "?to=" + to.encodeForUrl()

    fun writeCommentTree(generator: JsonGenerator, tree: CommentsTree, current: Commenter?) {
        generator.writeStartArray()
        tree.forEach { comment, answers ->
            generator.writeStartObject()

            writeCommentBody(generator, comment, current)

            generator.writeFieldName("answers")
            writeCommentTree(generator, answers, current)

            generator.writeEndObject()
        }
        generator.writeEndArray()
    }

    private fun writeCommentBody(generator: JsonGenerator, comment: Comment, current: Commenter?) {
        generator.writeFieldName("id")
        generator.writeString(comment.id.toString())

        generator.writeFieldName("authorSrc")
        generator.writeString(comment.authorSource.name)

        generator.writeFieldName("authorId")
        generator.writeString(comment.authorId)

        if (comment.removed) {
            generator.writeFieldName("removed")
            generator.writeBoolean(true)
        } else {
            generator.writeFieldName("text")
            generator.writeString(comment.text)

            if (current != null && current.canRemove(comment)) {
                generator.writeFieldName("canRemove")
                generator.writeBoolean(true)
            }
        }

        generator.writeFieldName("added")
        generator.writeNumber(comment.addedAt.toEpochMilli())
    }

    fun writeCommenter(generator: JsonGenerator, commenter: Commenter) {
        generator.writeStartObject()

        generator.writeFieldName("source")
        generator.writeString(commenter.source)

        generator.writeFieldName("id")
        generator.writeString(commenter.id)

        generator.writeFieldName("displayName")
        generator.writeString(commenter.displayName)

        generator.writeFieldName("avatarUrl")
        generator.writeString(commenter.avatarUrl)

        generator.writeFieldName("pageUrl")
        generator.writeString(commenter.pageUrl)

        generator.writeEndObject()
    }

    private val jackson = JsonFactory()
    fun sendHandler(): suspend ApplicationCall.(Unit) -> Unit = {
        if (request.requestedWith() != RequestedWith.XMLHttpRequest)
            throw BadRequestException("CSRF")

        val p = receiveParameters()
        val type = tables[p["type"] ?: throw BadRequestException("field 'type' id required")]
                ?: throw NotFoundException("no such type: '${p["type"]}'")
        val entityId = Uuid.fromString(p["id"] ?: throw BadRequestException("field 'id' is required"))
        val parentId = p["parentId"]?.let(UUID::fromString)
        val text = p["text"] ?: throw BadRequestException("field 'text' is required")

        val current = currentCommenter(this) ?: throw UnauthorizedException("no session for commenter")

        val comment = Comment(
                Uuid.randomUUID(), parentId, sources[current.source]!!, current.id, text, Instant.now(), false
        )
        dao.insert(type, entityId, comment)

        respondTextWriter {
            jackson.createGenerator(this).let { j ->
                j.writeStartObject()

                writeCommentBody(j, comment, current)

                j.writeFieldName("answers")
                j.writeStartArray()
                j.writeEndArray()

                j.writeEndObject()
                j.close()
            }
        }
    }

    fun deleteHandler(): suspend ApplicationCall.(Unit) -> Unit = {
        if (request.requestedWith() != RequestedWith.XMLHttpRequest)
            throw BadRequestException("CSRF")

        val commentId = receiveParameters()["id"]
                ?: throw BadRequestException("no required 'id' parameter")

        val current = currentCommenter(this)
                ?: throw UnauthorizedException("no session for commenter")

        val comment = dao.findById(Uuid.fromString(commentId))
                ?: throw  NotFoundException("no comment with id $commentId")

        if (!current.canRemove(comment))
            throw ForbiddenException("comment added by ${comment.authorSource.name}:${comment.authorId} cannot be removed by ${current.source}:${current.id}")

        dao.remove(comment)

        respond(HttpStatusCode.NoContent)
    }

    private fun Commenter.canRemove(comment: Comment) =
            this.source == comment.authorSource.name && this.id == comment.authorId

}
