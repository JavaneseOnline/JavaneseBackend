package online.javanese.locale

import online.javanese.sandbox.SandboxRunner

object SandboxRu : SandboxRunner.Messages {
    override val compiling: String get() = "Компиляция"
    override val compiled: String get() = "звавершена."
    override val deadline: String get() = "Превышено максимальное время выполнения."
}
