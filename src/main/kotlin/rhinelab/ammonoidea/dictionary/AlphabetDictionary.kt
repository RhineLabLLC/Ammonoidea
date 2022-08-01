package rhinelab.ammonoidea.dictionary

class AlphabetDictionary : IDictionary {
    private val cachedMixedCaseNames = mutableListOf<String>()
    private var index = 0
    override fun reset() {
        index = 0
    }

    override fun next(): String {
        return name(index++)
    }

    private fun name(index: Int): String {
        val cachedNames = cachedMixedCaseNames
        if (index < cachedNames.size) {
            return cachedNames[index]
        }
        val name = newName(index)
        cachedNames.add(index, name)
        return name
    }

    private fun newName(index: Int): String {
        val totalCharacterCount = 2 * 26
        val baseIndex = index / totalCharacterCount
        val offset = index % totalCharacterCount
        val newChar = charAt(offset)
        return if (baseIndex == 0) String(charArrayOf(newChar)) else name(baseIndex - 1) + newChar
    }

    private fun charAt(index: Int): Char {
        return ((if (index < 26) 'a' else 'A' - 26) + index)
    }
}