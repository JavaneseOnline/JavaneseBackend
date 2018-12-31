package online.javanese

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

// almost copy of org.apache.tomcat.util.http.fileupload.

private val logger = LoggerFactory.getLogger("FileExtensions")

@Throws(IOException::class)
fun File.deleteDirectory() {
    if (!exists()) {
        logger.warn("deleteDirectory(): {} does not exist", this)
        return
    }

    if (!isSymlink()) {
        cleanDirectory()
    }

    if (!delete()) {
        logger.error("deleteDirectory(): can't delete {}", this)
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
        throw IllegalArgumentException("$this does not exist")
    }

    if (!isDirectory) {
        throw IllegalArgumentException(toString() + " is not a directory")
    }

    val files = listFiles() ?: // null if security restricted
            throw IOException("Failed to list contents of $this")

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
