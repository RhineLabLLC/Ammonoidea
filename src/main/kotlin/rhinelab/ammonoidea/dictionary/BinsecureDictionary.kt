package rhinelab.ammonoidea.dictionary

class BinsecureDictionary : IDictionary {
    private var index = 0
    private val chars = charArrayOf(
        'c', '0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', 'a', 'b', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z')

    override fun next(): String {
        val charsetLength = chars.size
        var i = index
        val buf = CharArray(65)
        val negative = i < 0
        var charPos = 64
        if (!negative) {
            i = -i
        }
        while (i <= -charsetLength) {
            buf[charPos--] = chars[-(i % charsetLength)]
            i /= charsetLength
        }
        buf[charPos] = chars[-i]
        val s = String(buf, charPos, 65 - charPos)
        index++
        return s
    }


    override fun reset() {
        index = 0
    }
}