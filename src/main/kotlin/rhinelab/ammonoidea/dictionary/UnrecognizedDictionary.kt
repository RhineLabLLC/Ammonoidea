package rhinelab.ammonoidea.dictionary

class UnrecognizedDictionary : IDictionary {
    private var index = 0
    override fun next(): String {
        val charsetLength = CHARSET.size
        var i = index
        val buf = CharArray(33)
        val negative = i < 0
        var charPos = 32
        if (!negative) {
            i = -i
        }
        while (i <= -charsetLength) {
            buf[charPos--] = CHARSET[-(i % charsetLength)]
            i /= charsetLength
        }
        buf[charPos] = CHARSET[-i]
        val s = String(buf, charPos, 33 - charPos)
        index++
        return s
    }

    override fun reset() {
        index = 0
    }

    companion object {
        private val CHARSET = CharArray(33)

        init {
            for (i in CHARSET.indices) CHARSET[i] = ('\ua6ac'.toInt() + i).toChar()
        }
    }
}