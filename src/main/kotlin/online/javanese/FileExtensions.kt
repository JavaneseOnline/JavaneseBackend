package online.javanese

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

// almost copy of org.apache.tomcat.util.http.fileupload.

@Throws(IOException::class)
fun File.deleteDirectory() {
    if (!exists()) {
        return
    }

    if (!isSymlink()) {
        cleanDirectory()
    }

    if (!delete()) {
        throw IOException("Unable to delete directory $this.")
    }
}

@Throws(IOException::class)
fun File.isSymlink(): Boolean {
    if (File.separatorChar == '\\') {
        return false
    }

    val fileInCanonicalDir = if (parent == null) {
        this
    } else {
        val canonicalDir = parentFile.canonicalFile
        File(canonicalDir, name)
    }

    return fileInCanonicalDir.canonicalFile != fileInCanonicalDir.absoluteFile
}

@Throws(IOException::class)
fun File.cleanDirectory() {
    if (!exists()) {
        val message = toString() + " does not exist"
        throw IllegalArgumentException(message)
    }

    if (!isDirectory) {
        val message = toString() + " is not a directory"
        throw IllegalArgumentException(message)
    }

    val files = listFiles() ?: // null if security restricted
            throw IOException("Failed to list contents of " + this)

    var exception: IOException? = null
    for (file in files) {
        try {
            file.forceDelete()
        } catch (ioe: IOException) {
            exception = ioe
        }

    }

    if (null != exception) {
        throw exception
    }
}

@Throws(IOException::class)
fun File.forceDelete() {
    if (isDirectory) {
        deleteDirectory()
    } else {
        val filePresent = exists()
        if (!delete()) {
            if (!filePresent) {
                throw FileNotFoundException("File does not exist: " + this)
            }
            throw IOException("Unable to delete file: " + this)
        }
    }
}
