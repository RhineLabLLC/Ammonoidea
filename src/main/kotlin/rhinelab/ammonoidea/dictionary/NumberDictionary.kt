package rhinelab.ammonoidea.dictionary

class NumberDictionary : IDictionary {
    private var index = 0
    override fun next(): String {
        return index++.toString()
    }

    override fun reset() {
        index = 0
    }
}