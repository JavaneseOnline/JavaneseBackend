package online.javanese.template

class ErrorPageTemplate(
        private val resolveMessage: (key: String, default: String) -> String,
        private val render: (templateName: String, params: Map<String, Any>) -> String
) : (Int, String) -> String {

    override fun invoke(statusCode: Int, reason: String): String {

        return render(
                "error",
                mapOf(
                    "statusCode" to statusCode,
                    "reason" to resolveMessage("error.$statusCode.reason", reason)
                )
        )
    }

}
