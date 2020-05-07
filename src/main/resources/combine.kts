import java.io.File
import java.io.OutputStreamWriter
import java.net.URL


val nl = charArrayOf('\n')
val raw = "/home/miha/IdeaProjects/javanese/etc/static-raw/"
val prepared = "/home/miha/IdeaProjects/javanese/etc/static-prepared/"


combineAndUglifyMain("vue_zepto_mdl_dialog_scroll_unfocus_tabs_form_marked_comments")
combineAndUglifySandbox("highlight_trace_codemirror_clike_sandbox_switcher")
scssAndCsso("css/main")
scssAndCsso("sandbox/codemirror_ambiance")


fun combineAndUglifyMain(fileName: String) {
    jsFile(prepared + fileName).assertNotExists().create().writer().use {
        appendFrom(URL("https://cdnjs.cloudflare.com/ajax/libs/vue/2.5.22/vue.min.js"), it)
        appendFrom(URL("https://zeptojs.com/zepto.min.js"), it)
        appendFrom(URL("https://raw.githubusercontent.com/madrobby/zepto/master/src/fx.js"), it)
        appendFrom(URL("https://raw.githubusercontent.com/madrobby/zepto/master/src/fx_methods.js"), it)

        it.write(
                URL("https://raw.githubusercontent.com/suprMax/ZeptoScroll/master/static/zepto.scroll.js")
                        .openConnection().getInputStream().reader().readText()
                        .replace("$.os.android ? 1 : 0", "0") // we're not using Zepto.detect
        )
        it.write(nl)

        appendFrom(URL("https://code.getmdl.io/1.1.3/material.min.js"), it) // TODO: upgrade whole MDL
        appendFrom(URL("https://raw.githubusercontent.com/GoogleChrome/dialog-polyfill/master/dist/dialog-polyfill.js"), it)
        appendFrom(File(raw + "js/scroll_unfocus_tabs.js"), it)
        appendFrom(File(raw + "js/form.js"), it)

        appendFrom(URL("https://unpkg.com/marked@0.6.0/lib/marked.js"), it)
        appendFrom(File(raw + "js/comments.js"), it)
    }
    minifyJs(prepared + fileName)
}

fun combineAndUglifySandbox(fileName: String) {
    jsFile(prepared + fileName).assertNotExists().create().writer().use {
        appendFrom(File(raw + "js/highlight.pack.js"), it)
        appendFrom(File(raw + "js/trace.js"), it)
        appendFrom(URL("https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.33.0/codemirror.min.js"), it)
        appendFrom(URL("https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.33.0/mode/clike/clike.min.js"), it)
        appendFrom(File(raw + "sandbox/sandbox.js"), it)
        appendFrom(URL("https://raw.githubusercontent.com/dcompute/Zepto-Cookie/master/zepto.cookie.min.js"), it)
        appendFrom(File(raw + "js/programming-language-switcher.js"), it)
    }
    minifyJs(prepared + fileName)
}

fun jsFile(name: String) = File("$name.js")
fun minJsFile(name: String) = File("$name.min.js")

fun scssFile(name: String) = File("$name.scss")
fun cssFile(name: String) = File("$name.css")
fun cssMapFile(name: String) = File("$name.css.map")
fun minCssFile(name: String) = File("$name.min.css")

fun String.withoutPrecedingDirs() = split('/').last()

fun scssAndCsso(fileName: String) {
    val cssFile = cssFile(prepared + fileName.withoutPrecedingDirs()).assertNotExists().apply { parentFile.mkdirs() }
    val minCssFile = minCssFile(prepared + fileName.withoutPrecedingDirs())

    val scssCmd = arrayOf("scss", scssFile(raw + fileName).path, cssFile.path)
    printSh(scssCmd)
    check(Runtime.getRuntime().exec(scssCmd).waitFor() == 0)
    check(cssMapFile(prepared + fileName.withoutPrecedingDirs()).delete())

    val cssoCmd = arrayOf("csso", cssFile.path, minCssFile.path)
    printSh(cssoCmd)
    check(Runtime.getRuntime().exec(cssoCmd).waitFor() == 0)
    check(cssFile.delete())

    print(minCssFile.path)
    println(" has been created.\n")
}

fun File.assertNotExists() = apply { check(!exists()) }
fun File.create() = apply { parentFile.mkdirs(); check(createNewFile()) }

fun appendFrom(address: URL, output: OutputStreamWriter) {
    print("Downloading from $address...")
    address.openConnection().getInputStream().reader().copyTo(output)
    output.write(nl)
    println(" Done.")
}

fun appendFrom(file: File, output: OutputStreamWriter) {
    print("Appending from $file...")
    file.reader().use { it.copyTo(output) }
    output.write(nl)
    println(" Done.")
}

fun minifyJs(name: String) {
    val src = jsFile(name)

    val minified = minJsFile(name)
    if (minified.exists() && minified.isFile) {
        check(minified.delete())
    }
    val cmd = arrayOf("uglifyjs", "-o", minified.path, src.path)
    printSh(cmd)
    Runtime.getRuntime().exec(cmd).run {
        val out = inputStream.reader().readLines()
        val err = errorStream.reader().readLines()
        val code = waitFor()
        if (code != 0) {
            System.err.println("Uglify exited with code $code")
            out.forEach(System.out::println)
            err.forEach(System.err::println)
        }
    }

    print(minified.name)
    println(" has been created.\n")

    src.delete()
}

fun printSh(cmd: Array<out String>) {
    println(cmd.joinToString(" "))
}
