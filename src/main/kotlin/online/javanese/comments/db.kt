package online.javanese.comments

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.AbstractDao
import com.github.andrewoma.kwery.mapper.Column
import com.github.andrewoma.kwery.mapper.Converter
import com.github.andrewoma.kwery.mapper.SimpleConverter
import com.github.andrewoma.kwery.mapper.Table
import com.github.andrewoma.kwery.mapper.Value
import online.javanese.krud.kwery.Uuid
import online.javanese.model.idCol
import java.time.Instant


class Comment(
        val id: Uuid,
        val parentId: Uuid?,
        val authorSource: CommentsSource,
        val authorId: String, // specific for CommentsSource
        val text: String,
        val addedAt: Instant,
        val removed: Boolean
)

private object NullableUuidConverter : SimpleConverter<Uuid?>({ row, name -> row.objectOrNull(name) as Uuid? })

class CommentTable(
        private val commentsSources: Map<String, CommentsSource>
) : Table<Comment, Uuid>("comments") {

    val Id by idCol(Comment::id)
    val ParentId by col(Comment::parentId, name = "parentId", converter = NullableUuidConverter)
    val AuthorSource by col(Comment::authorSource, name = "authorSource", converter = Converter(
            { row, name -> commentsSources[row.string(name)]!! },
            { _, source -> source.name }
    ), default = CommentsSource("<error>", null) { error("damn, screw these default values!") }) // fixme: kwery sucks
    val AuthorId by col(Comment::authorId, name = "authorId")
    val Text by col(Comment::text, name = "text")
    val AddedAt by col(Comment::addedAt, name = "addedAt")
    val Removed by col(Comment::removed, name = "removed")

    override fun idColumns(id: Uuid): Set<Pair<Column<Comment, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Comment>): Comment = Comment(
            id = value of Id,
            parentId = value of ParentId,
            authorSource = value of AuthorSource,
            authorId = value of AuthorId,
            text = value of Text,
            addedAt = value of AddedAt,
            removed = value of Removed
    )

}

interface CommentsTree : Map<Comment, CommentsTree>
private class LinkedCommentsTree : LinkedHashMap<Comment, CommentsTree>(), CommentsTree

class CommentsFor<E>(
        val joinTableName: String,
        val idOf: (E) -> Uuid
)

class CommentDao(session: Session, table: CommentTable) : AbstractDao<Comment, Uuid>(session, table, table.Id.property) {

    fun <E> findTree(join: CommentsFor<E>, entity: E): CommentsTree =
            session.select(
                    sql = """SELECT c.* from "${join.joinTableName}" ec """ +
                            """INNER JOIN "comments" c ON ec."commentId" = c."id" """ +
                            """WHERE ec."entityId" = :entityId AND "removed" = false AND "parentId" IS NULL """ +
                            """ORDER BY "addedAt" ASC""",
                    mapper = table.rowMapper(),
                    parameters = mapOf("entityId" to join.idOf(entity))
            ).associateWithTo(LinkedCommentsTree(), ::subComments)

    private fun subComments(parent: Comment): CommentsTree =
            session.select(
                    sql = """SELECT * from "comments" WHERE "parentId" = :parentId""",
                    mapper = table.rowMapper(),
                    parameters = mapOf("parentId" to parent.id)
            ).associateWithTo(LinkedCommentsTree(), ::subComments)

    fun <E> insert(join: CommentsFor<E>, entityId: Uuid, comment: Comment) {
        withTransaction {
            insert(comment)
            session.update(
                    """INSERT INTO "${join.joinTableName}" ("entityId", "commentId") VALUES (:e, :c)""",
                    mapOf("e" to entityId, "c" to comment.id)
            )
        }
    }

    fun remove(comment: Comment) {
        val updated = session.update(
                """UPDATE "${table.name}" SET "removed" = true WHERE "id" = :id """,
                mapOf("id" to comment.id)
        )
        check(updated == 1) { "expected 1 updated row, got $updated" }
    }

}

/*
CREATE TABLE public.comments (
	id uuid NOT NULL,
	parentId uuid NULL,
	authorSource varchar(16) NOT NULL,
	authorId varchar(256) NOT NULL,
	text text NOT NULL,
	addedAt timestamp NOT NULL,
	removed bool NOT NULL,
	CONSTRAINT comments_pk PRIMARY KEY (id)
    CONSTRAINT comments_parent_fk FOREIGN KEY (parentId) REFERENCES public.comments(id)
);
CREATE INDEX comments_parentId_idx ON public.comments (parentId) WHERE parentId is not null;
*/
