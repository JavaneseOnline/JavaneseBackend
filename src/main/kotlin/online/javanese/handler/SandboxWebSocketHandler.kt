package online.javanese.handler

import checker.*
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.difflib.text.DiffRow
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.experimental.CancellationException
import online.javanese.Config
import online.javanese.krud.kwery.Uuid
import online.javanese.model.Task
import online.javanese.model.TaskDao
import online.javanese.sandbox.SandboxRunner
import java.io.IOException
import java.nio.ByteBuffer
import com.github.difflib.text.DiffRowGenerator
import io.ktor.util.escapeHTML


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

        val expected = technical.expectedOutput.split("\r\n")
        var linesWritten = 0
        SandboxRunner(
                javaLocation = config.sandboxJavaLocation,
                sandboxLocation = config.sandboxLocation,
                sandboxCommonsCli = config.sandboxCommonsCli,
                className = technical.compiledClassName,
                sourceCode = task.surroundSource(source),
                memoryLimit = technical.memoryLimit,
                timeLimit = technical.timeLimit,
                allowIn = technical.allowSystemIn,
                onProcessEvent = { type, payload ->

                    if (type == SandboxRunner.EventType.Out) {
                        output.append(payload).append('\n')
                        val expectedIdx = linesWritten++
                        if (expectedIdx in expected.indices) {
                            sendMessage(RuntimeMessage(type, diff(expected[expectedIdx], payload)))
                        } else {
                            sendMessage(RuntimeMessage(type, "<span class=\"editNewInline\">$payload</span>"))
                        }
                    } else if (type == SandboxRunner.EventType.Exit && payload == "0") {
                        sendMessage(RuntimeMessage(type, payload.escapeHTML()))
                        sendMessage(
                                if (task.isOutputCorrect(output))
                                    RuntimeMessage.CORRECT_SOLUTION
                                else
                                    RuntimeMessage.ILLEGAL_OUTPUT
                        )
                    } else {
                        sendMessage(RuntimeMessage(type, payload.escapeHTML()))
                    }
                }
        ).run()
    } catch (e: CheckerException) {
        sendMessage(CheckerMessage(e))
    } catch (ignored: CancellationException) {
    } catch (e: Throwable) {
        sendMessage(RuntimeMessage(SandboxRunner.EventType.Err, e.message ?: e.toString()))
    }

}

private val diffGenerator =
        DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(false)
                .newTag { if (it) "<s><span class=\"editNewInline\">" else "</span></s>" }
                .build()

private fun diff(expected: String, actual: String): String {
    val diff = diffGenerator
            .generateDiffRows(listOf(expected), listOf(actual))

    return diff.joinToString("\n", transform = DiffRow::getOldLine)
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
        IllegalOutput, CorrectSolution
    }

    internal companion object {
        internal val CORRECT_SOLUTION = RuntimeMessage(MessageType.CorrectSolution, null)
        internal val ILLEGAL_OUTPUT = RuntimeMessage(MessageType.IllegalOutput, null)
    }
}

private class CheckerMessage internal constructor(e: Exception) {

    val type: MessageType
    val name: String?
    val value: String?

    init {
        when (e) {
            is NoRequiredVariableException -> {
                type = MessageType.NoVar
                name = e.name
                value = e.type
            }
            is NoRequiredEquationException -> {
                type = MessageType.NoEq
                name = e.`var`
                value = e.`val`.toString()
            }
            is NotMatchesToRequiredPatternException -> {
                type = MessageType.NotMatches
                name = null
                value = null
            }
            else -> throw IllegalArgumentException("given exception of unsupported subtype of CheckerMessage: " + e)
        }
    }

    private enum class MessageType {
        NoVar, NoEq, NotMatches
    }
}

fun Task.surroundSource(source: String): String {

    val tech = technicalInfo
    val withAppended = if (tech.codeToAppend.isNotBlank())
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
