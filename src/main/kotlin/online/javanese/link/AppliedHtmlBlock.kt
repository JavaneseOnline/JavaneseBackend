package online.javanese.link

import kotlinx.html.FlowContent


class AppliedHtmlBlock1<in T>(
        private val block: HtmlBlock1<T>,
        private val t: T
) : HtmlBlock {

    override fun render(where: FlowContent, classes: String?) {
        block.render(where, t, classes)
    }

}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> HtmlBlock1<T>.invoke(t: T) =
        AppliedHtmlBlock1(this, t)



class AppliedHtmlBlock2<in T, in U>(
        private val block: HtmlBlock2<T, U>,
        private val t: T,
        private val u: U
) : HtmlBlock {

    override fun render(where: FlowContent, classes: String?) {
        block.render(where, t, u, classes)
    }

}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T, U> HtmlBlock2<T, U>.invoke(t: T, u: U) =
        AppliedHtmlBlock2(this, t, u)



class AppliedHtmlBlock3<in T, in U, in V>(
        private val block: HtmlBlock3<T, U, V>,
        private val t: T,
        private val u: U,
        private val v: V
) : HtmlBlock {

    override fun render(where: FlowContent, classes: String?) {
        block.render(where, t, u, v, classes)
    }

}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T, U, V> HtmlBlock3<T, U, V>.invoke(t: T, u: U, v: V) =
        AppliedHtmlBlock3(this, t, u, v)



class AppliedHtmlBlock4<in T, in U, in V, in W>(
        private val block: HtmlBlock4<T, U, V, W>,
        private val t: T,
        private val u: U,
        private val v: V,
        private val w: W
) : HtmlBlock {

    override fun render(where: FlowContent, classes: String?) {
        block.render(where, t, u, v, w, classes)
    }

}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T, U, V, W> HtmlBlock4<T, U, V, W>.invoke(t: T, u: U, v: V, w: W) =
        AppliedHtmlBlock4(this, t, u, v, w)
