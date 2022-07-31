import rhinelab.ammonoidea.process
import java.io.File

fun main() {
    process(File("IsolatedTest.class").readBytes(), true)
}