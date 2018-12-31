package online.javanese

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.util.StatusPrinter


class Logback : ContextAwareBase(), Configurator {

    override fun configure(loggerContext: LoggerContext) {
        val logEncoder = PatternLayoutEncoder()
        logEncoder.context = loggerContext
        logEncoder.pattern = "%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n"
        logEncoder.start()

        val logConsoleAppender = ConsoleAppender<ILoggingEvent>()
        logConsoleAppender.context = loggerContext
        logConsoleAppender.name = "console"
        logConsoleAppender.encoder = logEncoder
        logConsoleAppender.start()

        val fileAppender = FileAppender<ILoggingEvent>()
        fileAppender.context = loggerContext
        fileAppender.file = "javanese.log"
        fileAppender.name = "fileAppender"
        fileAppender.encoder = logEncoder
        fileAppender.isAppend = true
        fileAppender.start()

        val log = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
        log.level = Level.INFO
        log.addAppender(logConsoleAppender)
        log.addAppender(fileAppender)

        StatusPrinter.print(loggerContext)
    }

}
