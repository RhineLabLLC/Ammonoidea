package rhinelab.ammonoidea.dictionary

interface IDictionary {
    fun reset()

    fun next(): String
}