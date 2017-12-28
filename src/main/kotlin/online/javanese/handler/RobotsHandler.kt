package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.response.respondText
import online.javanese.Config


fun RobotsHandler(config: Config): suspend (ApplicationCall) -> Unit = { call ->
    call.respondText(
            """User-agent: *
            |Disallow:
            |Sitemap: ${config.siteUrl}/sitemap.xml
            |Host: ${config.siteHost}
            |""".trimMargin())
}
