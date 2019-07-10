package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.util.toMap
import io.ktor.util.valuesOf
import online.javanese.exception.UnauthorizedException
import online.javanese.krud.kwery.kweryEntityMapping.MapToKweryEntityMapper
import online.javanese.model.CodeReviewCandidateDao
import online.javanese.model.CodeReviewCandidateTable
import online.javanese.social.UserSessions


fun SubmitCodeReviewCandidateHandler(
        reviewCandidateDao: CodeReviewCandidateDao,
        sessions: UserSessions
): suspend (ApplicationCall, Unit) -> Unit {

    val valuesToTaskErrorReport =
            MapToKweryEntityMapper(CodeReviewCandidateTable) {
                when (it) {
                    "id" -> "id"
                    "senderName" -> "name"
                    "problemStatement" -> "text"
                    "code" -> "code"
                    "senderContact" -> "email"
                    "senderAuth" -> "senderAuth"
                    else -> throw IllegalArgumentException("unexpected key: $it")
                }
            }


    return { call, _ ->
        val user = sessions.currentUser(call) ?: throw UnauthorizedException("no user")
        val values = valuesOf(call.receiveParameters().toMap() + ("senderAuth" to listOf("${user.source}:${user.displayName}")))
        val reviewCandidate = valuesToTaskErrorReport(values)
        reviewCandidateDao.insert(reviewCandidate)
        call.respond(HttpStatusCode.NoContent, "")
    }
}
