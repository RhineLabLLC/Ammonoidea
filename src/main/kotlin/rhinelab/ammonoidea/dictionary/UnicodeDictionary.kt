package rhinelab.ammonoidea.dictionary

object UnicodeDictionary : IDictionary {
    private val CHARSET = "\u00c3\u00c9\u0152\u017d\u00ad\uccccAEi".toCharArray()
    private val cache: MutableSet<String> = HashSet()
    private var cachedLength = 0
    var length = 8
    override fun next(): String {
        if (cachedLength > length) length = cachedLength
        var count = 0
        val arrLen = CHARSET.size - 1
        var s: String
        do {
            s = randomString(length)
            if (count++ >= arrLen) {
                length++
                count = 0
            }
        } while (cache.contains(s))
        cache.add(s)
        cachedLength = length
        return s
    }

    override fun reset() {
        cache.clear()
    }

    fun randomString(length: Int): String {
        val c = CharArray(length)
        for (i in 0 until length) c[i] = CHARSET[(CHARSET.indices).random()]
        return String(c)
    }

}