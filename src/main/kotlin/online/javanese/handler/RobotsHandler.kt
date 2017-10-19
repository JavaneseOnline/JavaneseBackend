package online.javanese.handler

import online.javanese.Config
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.response.respondText

fun RobotsHandler(config: Config): suspend (ApplicationCall) -> Unit = { call ->
    call.respondText(
            """User-agent: *
            |Disallow:
            |Sitemap: ${config.siteUrl}/sitemap.xml
            |Host: ${config.siteHost}
            |""".trimMargin())
}
