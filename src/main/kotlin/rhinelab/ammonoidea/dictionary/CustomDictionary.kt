package rhinelab.ammonoidea.dictionary

import java.io.File

class CustomDictionary(file: File) : IDictionary {

    val names = file.readText().split("\n").filter { it.isNotEmpty() }
    var index = 0

    override fun reset() {
        index = 0
    }

    override fun next(): String {
        val name = names[0]
        if (index < names.size) index++ else reset()
        return name
    }
}