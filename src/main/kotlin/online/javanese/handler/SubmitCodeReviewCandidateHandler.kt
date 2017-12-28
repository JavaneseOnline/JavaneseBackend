package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import online.javanese.krud.kwery.kweryEntityMapping.MapToKweryEntityMapper
import online.javanese.krud.toStringMap
import online.javanese.model.CodeReviewCandidateDao
import online.javanese.model.CodeReviewCandidateTable


fun SubmitCodeReviewCandidateHandler(
        reviewCandidateDao: CodeReviewCandidateDao
): suspend (ApplicationCall) -> Unit {

    val valuesToTaskErrorReport =
            MapToKweryEntityMapper(CodeReviewCandidateTable) {
                when (it) {
                    "id" -> "id"
                    "senderName" -> "name"
                    "problemStatement" -> "text"
                    "code" -> "code"
                    "senderContact" -> "email"
                    else -> throw IllegalArgumentException("unexpected key: $it")
                }
            }

    return { call ->
        val valuesMap = call.receiveParameters()
        val reviewCandidate = valuesToTaskErrorReport(valuesMap.toStringMap())
        reviewCandidateDao.insert(reviewCandidate)
        call.respondText("ok", ContentType.Text.Plain)
    }
}
