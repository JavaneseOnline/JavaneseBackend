package online.javanese


class Just<R>(val value: R) : () -> R, (Any?) -> R, (Any?, Any?) -> R {

    override fun invoke(): R = value
    override fun invoke(p1: Any?): R = value
    override fun invoke(p1: Any?, p2: Any?): R = value

    override fun toString(): String = "(...) -> $value"

}

@Suppress("UNCHECKED_CAST")
fun <T> Identity(): (T) -> T =
        IdentityFunc as (T) -> T

private object IdentityFunc : (Any?) -> Any? {

    override fun invoke(p1: Any?): Any? = p1

    override fun toString(): String = "(p) -> p"

}
