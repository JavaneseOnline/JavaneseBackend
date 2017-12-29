import java.io.File
import java.io.OutputStreamWriter
import java.net.URL


val nl = charArrayOf('\n')

val mainScript = "js/vue_zepto_mdl_dialog_highlight_trace_scroll_unfocus_tabs_form"
val sandboxScript = "sandbox/codemirror_clike_sandbox"

val mainStyle = "css/main"
val codeMirrorStyle = "sandbox/codemirror_ambiance"


combineAndUglifyMain(mainScript)
combineAndUglifySandbox(sandboxScript)
scssAndCsso(mainStyle)
scssAndCsso(codeMirrorStyle)


fun combineAndUglifyMain(fileName: String) {
    jsFile(fileName).assertNotExists().writer().use {
                appendFrom(URL("https://cdnjs.cloudflare.com/ajax/libs/vue/1.0.25/vue.min.js"), it) //todo: upgrade
                appendFrom(URL("http://zeptojs.com/zepto.min.js"), it)
                appendFrom(URL("https://raw.githubusercontent.com/madrobby/zepto/master/src/fx.js"), it)
                appendFrom(URL("https://raw.githubusercontent.com/madrobby/zepto/master/src/fx_methods.js"), it)

                it.write(
                        URL("https://raw.githubusercontent.com/suprMax/ZeptoScroll/master/static/zepto.scroll.js")
                                .openConnection().getInputStream().reader().readText()
                                .replace("$.os.android ? 1 : 0", "0") // we're not using Zepto.detect
                )
                it.write(nl)

                appendFrom(URL("https://code.getmdl.io/1.1.3/material.min.js"), it) // TODO: upgrade whole MDL
                appendFrom(URL("https://raw.githubusercontent.com/GoogleChrome/dialog-polyfill/master/dialog-polyfill.js"), it)
                appendFrom(File("js/trace.js"), it)
                appendFrom(File("js/highlight.pack.js"), it)
                appendFrom(File("js/scroll_unfocus_tabs.js"), it)
                appendFrom(File("js/form.js"), it)
            }
    minifyJs(fileName)
}

fun combineAndUglifySandbox(fileName: String) {
    jsFile(fileName).assertNotExists().writer().use {
                appendFrom(URL("https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.33.0/codemirror.min.js"), it)
                appendFrom(URL("https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.33.0/mode/clike/clike.min.js"), it)
                appendFrom(File("sandbox/sandbox.js"), it)
            }
    minifyJs(fileName)
}

fun jsFile(name: String) = File("$name.js")
fun minJsFile(name: String) = File("$name.min.js")

fun scssFile(name: String) = File("$name.scss")
fun cssFile(name: String) = File("$name.css")
fun cssMapFile(name: String) = File("$name.css.map")
fun minCssFile(name: String) = File("$name.min.css")

fun scssAndCsso(fileName: String) {
    val cssFile = cssFile(fileName).assertNotExists()
    val minCssFile = minCssFile(fileName)
    println("Running SCSS...")
    check(Runtime.getRuntime().exec(arrayOf("scss", scssFile(fileName).path, cssFile.path)).waitFor() == 0)
    check(cssMapFile(fileName).delete())
    println("Running CSSO...")
    check(Runtime.getRuntime().exec(arrayOf("csso", cssFile.path, minCssFile.path)).waitFor() == 0)
    check(cssFile.delete())
    print(minCssFile.path)
    println(" has been created.")
}

fun File.assertNotExists() = apply { check(!exists()) }

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

    println("Running UglifyJS...")
    val minified = minJsFile(name)
    if (minified.exists() && minified.isFile) {
        check(minified.delete())
    }
    check(Runtime.getRuntime().exec(arrayOf("uglifyjs", "-o", minified.path, src.path)).waitFor() == 0)

    print(minified.name)
    println(" has been created.")

    src.delete()
}
