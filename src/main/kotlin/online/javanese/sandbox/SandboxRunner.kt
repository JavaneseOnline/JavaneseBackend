package online.javanese.sandbox

import kotlinx.coroutines.experimental.*
import online.javanese.deleteDirectory
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import javax.tools.ToolProvider

/**
 * Created by miha on 19.04.16
 */
class SandboxRunner(
        private val javaLocation: String,
        private val sandboxLocation: String,
        sandboxCommonsCli: String,
        private val className: String,
        private val sourceCode: String,
        private val memoryLimit: Int,
        private val timeLimit: Int,
        private val allowIn: Boolean,
        private val messages: Messages,
        private val onProcessEvent: suspend (EventType, payload: String) -> Unit = { _, _ ->  }
) {

    // todo: make sandbox without CommonsCli
    private val classpath: String = sandboxLocation + File.pathSeparator + sandboxCommonsCli

    @Volatile private var procStdin: OutputStreamWriter? = null

    @Throws(IOException::class)
    suspend fun run() {

        // todo: Java 9 contains REPL. Is there any API to evaluate arbitrary code?

        val classFile = compile()
        try {

            /*Process javap = Runtime.getRuntime().exec(
                    new String[]{"javap", "-c", classFile.getName()}, null, classFile.getParentFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(javap.getInputStream()));
            System.out.println("  ==== javap ====");
            while (javap.isAlive()) {
                while (reader.ready()) {
                    System.out.println(reader.readLine());
                }
            }
            System.out.println(" ===============");*/

            // run
            val proc = Runtime.getRuntime().exec(createJavaArgs(classFile.file), null, classFile.dir.parentFile)
            val stdout = proc.inputStream.bufferedReader()
            val stderr = proc.errorStream.bufferedReader()
            procStdin = proc.outputStream.writer()

            withTimeout((VM_START_TIME + timeLimit).toLong(), TimeUnit.SECONDS) {
                // talk with process
                while (proc.isAlive || stdout.ready() || stderr.ready()) {
                    try {
                        communicate(stdout, stderr)
                    } catch (e: CancellationException) {
                        proc.destroyForcibly()
                        onProcessEvent(EventType.DEADLINE, messages.deadline)
                        run(NonCancellable) {
                            communicate(stdout, stderr)
                        }
                    }
                }
            }
            procStdin = null

            // exit
            onProcessEvent(EventType.EXIT, proc.exitValue().toString())
        } finally {
            classFile.clear()
        }
    }

    private suspend fun communicate(stdout: BufferedReader, stderr: BufferedReader) {
        while (stdout.ready()) {
            val l = stdout.readLine()
            onProcessEvent(EventType.OUT, l)
        }
        while (stderr.ready()) {
            val l = stderr.readLine()
            onProcessEvent(EventType.ERR, l)
        }
        delay(100, TimeUnit.MILLISECONDS)
    }

    @Throws(IOException::class)
    private suspend fun compile(): CompiledClassFile {
        onProcessEvent(EventType.STATUS, messages.compiling)

        val sourceFileName = className + ".java"

        val compilationDir = File(sandboxLocation + "/" + UUID.randomUUID())
        if (!compilationDir.mkdir()) {
            throw IOException("can't mkdir()")
        }
        val sourceFile = File(compilationDir, sourceFileName)

        Files.write(Paths.get(sourceFile.absolutePath), sourceCode.toByteArray(charset("UTF-8")))

        val os = ByteArrayOutputStream()
        val javac = ToolProvider.getSystemJavaCompiler()
        val result = javac.run(null, null, os, sourceFile.absolutePath)

        if (result != 0) {
            onProcessEvent(EventType.ERR, String(os.toByteArray()).replace(compilationDir.absolutePath + "/", "…/"))
            onProcessEvent(EventType.EXIT, result.toString())
            throw IOException("compilation error.")
        }

        onProcessEvent(EventType.STATUS, messages.compiled)

        return CompiledClassFile(File(compilationDir, "$className.class"), compilationDir)
    }

    @Throws(IOException::class)
    private fun createJavaArgs(classFile: File): Array<String> {
        val argList = mutableListOf(
                javaLocation,
                "-cp", classpath,
                "-Xss170K",
                "-Xmx" + (VM_MIN_MEMORY + memoryLimit) + "M",
                "sandbox.Sandbox",
                className,
                classFile.canonicalPath
        )
        if (allowIn) {
            argList.add("--allowIn")
        }
        return argList.toTypedArray()
    }

    // fixme: starting runner should return object which will be responsible for holding stdout, etc
    @Throws(IOException::class)
    fun writeToProcess(line: String) {
        val stdin = procStdin
                ?: throw IllegalStateException("Process is not running.")

        stdin.write(line)
        stdin.write(ENDL)
        stdin.flush()
    }

    private class CompiledClassFile internal constructor(
            internal val file: File,
            internal val dir: File
    ) {

        @Throws(IOException::class)
        fun clear() {
            dir.deleteDirectory()
        }
    }

    interface Messages {
        val deadline: String
        val compiling: String
        val compiled: String
    }

    enum class EventType {
        STATUS, OUT, ERR, EXIT, DEADLINE
    }

    private companion object {
        private val ENDL = charArrayOf('\n')
        private const val VM_MIN_MEMORY = 4
        private const val VM_START_TIME = 5
    }

}
