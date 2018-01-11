package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.AbstractDao
import com.github.andrewoma.kwery.mapper.Column
import com.github.andrewoma.kwery.mapper.Table
import com.github.andrewoma.kwery.mapper.Value
import online.javanese.krud.kwery.Uuid


class CodeReviewCandidate(
        val id: Uuid,
        val senderName: String,
        val problemStatement: String,
        val code: String,
        val senderContact: String
)

object CodeReviewCandidateTable : Table<CodeReviewCandidate, Uuid>("codeReviewCandidates") {

    val Id by idCol(CodeReviewCandidate::id)
    val SenderName by col(CodeReviewCandidate::senderName, name = "senderName")
    val ProblemStatement by col(CodeReviewCandidate::problemStatement, name = "problemStatement")
    val Code by col(CodeReviewCandidate::code, name = "code")
    val SenderContact by col(CodeReviewCandidate::senderContact, name = "senderContact")


    override fun idColumns(id: Uuid): Set<Pair<Column<CodeReviewCandidate, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<CodeReviewCandidate>): CodeReviewCandidate = CodeReviewCandidate(
            id = value of Id,
            senderName = value of SenderName,
            problemStatement = value of ProblemStatement,
            code = value of Code,
            senderContact = value of SenderContact
    )

}

class CodeReviewCandidateDao(
        session: Session
) : AbstractDao<CodeReviewCandidate, Uuid>(session, CodeReviewCandidateTable, CodeReviewCandidateTable.Id.property) {

    private val insertSql = insertSql(table)

    fun insert(value: CodeReviewCandidate): CodeReviewCandidate =
            insert(table, session, insertSql, value)

}

/*
CREATE TABLE public."codeReviewCandidates" (
	"id" uuid NOT NULL,
	"senderName" varchar(256) NOT NULL,
	"problemStatement" text NOT NULL,
	"code" text NOT NULL,
	"senderContact" varchar(256) NOT NULL,
	CONSTRAINT codereviewcandidates_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
