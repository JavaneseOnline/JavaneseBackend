package online.javanese.handler

import online.javanese.kweryEntityMapping.ValuesMapToKweryEntityMapper
import online.javanese.model.CodeReviewCandidateDao
import online.javanese.model.CodeReviewCandidateTable
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.request.receiveParameters
import org.jetbrains.ktor.response.respondText

fun SubmitCodeReviewCandidateHandler(
        reviewCandidateDao: CodeReviewCandidateDao
): suspend (ApplicationCall) -> Unit {

    val valuesToTaskErrorReport =
            ValuesMapToKweryEntityMapper(CodeReviewCandidateTable) {
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
        val reviewCandidate = valuesToTaskErrorReport(valuesMap)
        reviewCandidateDao.insert(reviewCandidate)
        call.respondText("ok", ContentType.Text.Plain)
    }
}
