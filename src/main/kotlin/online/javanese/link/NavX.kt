package online.javanese.link

import kotlinx.html.FlowContent
import kotlinx.html.nav


typealias BetweenBlocks = FlowContent.() -> Unit


class Nav1<in T>(
        private val block: HtmlBlock1<T>
) : HtmlBlock1<T> {

    override fun render(where: FlowContent, param: T, classes: String?) = with(where) {
        nav(classes = classes) {
            block.render(this, param)
        }
    }

}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> navOf(block1: Link<T, *>) = Nav1(block1)



class Nav2<in T, in U>(
        private val block1: HtmlBlock1<T>,
        private val block2: HtmlBlock1<U>,
        private val betweenBlocks: BetweenBlocks
) : HtmlBlock2<T, U> {

    override fun render(where: FlowContent, param1: T, param2: U, classes: String?) = with(where) {
        nav(classes = classes) {
            block1.render(this, param1)
            betweenBlocks()
            block2.render(this, param2)
        }
    }

}

@Suppress("NOTHING_TO_INLINE")
inline fun <T, U> BetweenBlocks.navOf(block1: Link<T, *>, block2: Link<U, *>) =
        Nav2(block1, block2, this)



class Nav3<in T, in U, in V>(
        private val block1: Link<T, *>,
        private val block2: Link<U, *>,
        private val block3: Link<V, *>,
        private val betweenBlocks: BetweenBlocks
) : HtmlBlock3<T, U, V> {

    override fun render(where: FlowContent, param1: T, param2: U, param3: V, classes: String?) = with(where) {
        nav(classes = classes) {
            block1.render(this, param1)
            betweenBlocks()
            block2.render(this, param2)
            betweenBlocks()
            block3.render(this, param3)
        }
    }

}

@Suppress("NOTHING_TO_INLINE")
inline fun <T, U, V> BetweenBlocks.navOf(block1: Link<T, *>, block2: Link<U, *>, block3: Link<V, *>) =
        Nav3(block1, block2, block3, this)



class Nav4<in T, in U, in V, in W>(
        private val block1: Link<T, *>,
        private val block2: Link<U, *>,
        private val block3: Link<V, *>,
        private val block4: Link<W, *>,
        private val betweenBlocks: BetweenBlocks
) : HtmlBlock4<T, U, V, W> {

    override fun render(where: FlowContent, param1: T, param2: U, param3: V, param4: W, classes: String?) = with(where) {
        nav(classes = classes) {
            block1.render(this, param1)
            betweenBlocks()
            block2.render(this, param2)
            betweenBlocks()
            block3.render(this, param3)
            betweenBlocks()
            block4.render(this, param4)
        }
    }

}

@Suppress("NOTHING_TO_INLINE")
inline fun <T, U, V, W> BetweenBlocks.navOf(block1: Link<T, *>, block2: Link<U, *>, block3: Link<V, *>, block4: Link<W, *>) =
        Nav4(block1, block2, block3, block4, this)
