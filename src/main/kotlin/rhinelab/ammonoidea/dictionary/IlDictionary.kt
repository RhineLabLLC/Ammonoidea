package rhinelab.ammonoidea.dictionary

class IlDictionary : IDictionary {
    var CHARSET = "Ili1".toCharArray()
    private val cache: MutableSet<String> = HashSet()
    private var cachedLength = 0
    var length = 8
    override fun next(): String {
        if (cachedLength > length) length = cachedLength
        var count = 0
        val arrLen = CHARSET.size
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
        for (i in 0 until length) c[i] = CHARSET[(0..CHARSET.size).random()]
        return String(c)
    }
}