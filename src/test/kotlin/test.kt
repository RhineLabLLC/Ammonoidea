import rhinelab.ammonoidea.processSingleClass
import java.io.File

fun main() {
    processSingleClass(File("IsolatedTest.class").readBytes(), false)
}