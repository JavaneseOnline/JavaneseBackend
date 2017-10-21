package online.javanese.handler

import checker.*
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.CancellationException
import online.javanese.Config
import online.javanese.Uuid
import online.javanese.locale.SandboxRu
import online.javanese.model.Task
import online.javanese.model.TaskDao
import online.javanese.sandbox.SandboxRunner
import org.jetbrains.ktor.websocket.DefaultWebSocketSession
import org.jetbrains.ktor.websocket.Frame
import org.jetbrains.ktor.websocket.WebSocketSession
import org.jetbrains.ktor.websocket.readText
import java.io.IOException
import java.nio.ByteBuffer

fun SandboxWebSocketHandler(
        config: Config,
        taskDao: TaskDao
): suspend DefaultWebSocketSession.() -> Unit = func@ {

    val task = taskDao.findById(Uuid.fromString(call.parameters["task"]!!))!!

    val source = (incoming.receive() as Frame.Text).readText()

    try {
        task.checkSource(source)

        val technical = task.technicalInfo

        val output = StringBuilder()

        SandboxRunner(
                javaLocation = config.sandboxJavaLocation,
                sandboxLocation = config.sandboxLocation,
                sandboxCommonsCli = config.sandboxCommonsCli,
                className = technical.compiledClassName,
                sourceCode = task.surroundSource(source),
                memoryLimit = technical.memoryLimit,
                timeLimit = technical.timeLimit,
                allowIn = technical.allowSystemIn,
                messages = SandboxRu,
                onProcessEvent = { type, payload ->
                    sendMessage(RuntimeMessage(type, payload))

                    if (type == SandboxRunner.EventType.EXIT) {
                        if (payload == "0") {
                            sendMessage(
                                    if (task.isOutputCorrect(output))
                                        RuntimeMessage.CORRECT_SOLUTION
                                    else
                                        RuntimeMessage(RuntimeMessage.MessageType.ILLEGAL_OUTPUT, technical.expectedOutput)
                            )
                        }
                    } else if (type == SandboxRunner.EventType.OUT) {
                        output.append(payload).append('\n')
                    }
                }
        ).run()
    } catch (e: CheckerException) {
        sendMessage(CheckerMessage(e))
    } catch (ignored: CancellationException) {
    } catch (e: Throwable) {
        sendMessage(RuntimeMessage(SandboxRunner.EventType.ERR, e.message ?: e.toString()))
    }

}

private val mapper = ObjectMapper()
private val reader = mapper.reader().forType(Requirements::class.java)
@Throws(CheckerException::class)
private fun Task.checkSource(source: String) { // TODO: AST instead of this shit
    val checkRules = technicalInfo.checkRules
    if (checkRules.isEmpty()) {
        return
    }

    try {
        val reqs = reader.readValue<Requirements>(checkRules)
        if (reqs.variables != null) {
            for (`var` in reqs.variables!!) {
                source.requireVariable(`var`.type, `var`.name)
            }
        }
        if (reqs.equations != null) {
            for (equation in reqs.equations!!) {
                source.requireEquation(equation.`var`, equation.type, equation.`val`)
            }
        }
        if (reqs.patterns != null) {
            for (pat in reqs.patterns!!) {
                source.requirePatternMatch(pat)
            }
        }
        if (reqs.rawPatterns != null) {
            for (pat in reqs.rawPatterns!!) {
                source.requireRawPatternMatch(pat)
            }
        }
    } catch (e: JsonProcessingException) {
        throw IllegalStateException(e)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }

}

private class RuntimeMessage internal constructor(
        val type: Enum<*>,
        val data: String?
) {
    internal enum class MessageType {
        ILLEGAL_OUTPUT, CORRECT_SOLUTION
    }

    internal companion object {
        internal val CORRECT_SOLUTION = RuntimeMessage(MessageType.CORRECT_SOLUTION, null)
    }
}

private class CheckerMessage internal constructor(e: Exception) {

    val type: MessageType
    val name: String?
    val value: String?

    init {
        when (e) {
            is NoRequiredVariableException -> {
                type = MessageType.NO_VAR
                name = e.name
                value = e.type
            }
            is NoRequiredEquationException -> {
                type = MessageType.NO_EQ
                name = e.`var`
                value = e.`val`.toString()
            }
            is NotMatchesToRequiredPatternException -> {
                type = MessageType.NOT_MATCHES
                name = null
                value = null
            }
            else -> throw IllegalArgumentException("given exception of unsupported subtype of CheckerMessage: " + e)
        }
    }

    private enum class MessageType {
        NO_VAR, NO_EQ, NOT_MATCHES
    }
}

fun Task.surroundSource(source: String): String {

    val tech = technicalInfo
    val withAppended = if (tech.codeToAppend.isEmpty())
        "$source\n\n${tech.codeToAppend}"
    else
        source

    return when (tech.inputMethod) {
        Task.InputMethod.MethodBody -> wrapIntoClass(tech.compiledClassName, wrapIntoMain(withAppended))
        Task.InputMethod.ClassBody -> wrapIntoClass(tech.compiledClassName, withAppended)
        Task.InputMethod.WholeClass -> withAppended
    }
}

private fun wrapIntoClass(className: String, code: String): String =
        "public class $className {\n$code\n}"

private fun wrapIntoMain(code: String): String =
        "public static void main() {\n$code\n}"

private suspend fun WebSocketSession.sendMessage(message: Any) {
    send(Frame.Text(true, ByteBuffer.wrap(mapper.writeValueAsString(message).toByteArray())))
}

fun Task.isOutputCorrect(output: CharSequence): Boolean =
        technicalInfo
                .expectedOutput
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim() == output.toString().trim()
