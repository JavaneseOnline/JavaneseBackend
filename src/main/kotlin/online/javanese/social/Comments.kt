package online.javanese.social

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.github.andrewoma.kwery.core.Session
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondTextWriter
import online.javanese.RequestedWith
import online.javanese.exception.BadRequestException
import online.javanese.exception.ForbiddenException
import online.javanese.exception.NotFoundException
import online.javanese.exception.UnauthorizedException
import online.javanese.krud.kwery.Uuid
import online.javanese.link.Action
import online.javanese.requestedWith
import java.time.Instant
import java.util.*


class Comments(
        session: Session,
        val userSessions: UserSessions,
        val commentAction: Action<Unit, *>,
        vararg supportedTables: CommentsFor<*>
) {

    private val dao = CommentDao(session, CommentTable(userSessions.sources))
    private val tables = supportedTables.associateBy(CommentsFor<*>::joinTableName)

    fun <E> fetch(join: CommentsFor<E>, entity: E): CommentsTree =
            dao.findTree(join, entity)

    fun writeCommentTree(generator: JsonGenerator, tree: CommentsTree, current: User?) {
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

    private fun writeCommentBody(generator: JsonGenerator, comment: Comment, current: User?) {
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

    fun writeUser(generator: JsonGenerator, user: User) {
        generator.writeStartObject()

        generator.writeFieldName("source")
        generator.writeString(user.source)

        generator.writeFieldName("id")
        generator.writeString(user.id)

        generator.writeFieldName("displayName")
        generator.writeString(user.displayName)

        generator.writeFieldName("avatarUrl")
        generator.writeString(user.avatarUrl)

        generator.writeFieldName("pageUrl")
        generator.writeString(user.pageUrl)

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

        val current = userSessions.currentUser(this) ?: throw UnauthorizedException("no session for user")

        val comment = Comment(
                Uuid.randomUUID(), parentId, userSessions.sources[current.source]!!, current.id, text, Instant.now(), false
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

        val current = userSessions.currentUser(this)
                ?: throw UnauthorizedException("no session for user")

        val comment = dao.findById(Uuid.fromString(commentId))
                ?: throw  NotFoundException("no comment with id $commentId")

        if (!current.canRemove(comment))
            throw ForbiddenException("comment added by ${comment.authorSource.name}:${comment.authorId} cannot be removed by ${current.source}:${current.id}")

        dao.remove(comment)

        respond(HttpStatusCode.NoContent)
    }

    private fun User.canRemove(comment: Comment) =
            this.source == comment.authorSource.name && this.id == comment.authorId

}
