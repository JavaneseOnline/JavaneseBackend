package online.javanese


class Just<R>(val value: R) : () -> R, (Any?) -> R, (Any?, Any?) -> R {

    override fun invoke(): R = value
    override fun invoke(p1: Any?): R = value
    override fun invoke(p1: Any?, p2: Any?): R = value

    override fun toString(): String = "(...) -> $value"

}
