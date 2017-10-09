package online.javanese

class Quadruple<out A, out B, out C, out D>(
        val a: A, val b: B, val c: C, val d: D
) {
    operator fun component1() = a
    operator fun component2() = b
    operator fun component3() = c
    operator fun component4() = d
}
