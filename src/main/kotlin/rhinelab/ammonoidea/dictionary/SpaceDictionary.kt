package rhinelab.ammonoidea.dictionary

import java.util.*

private val character = charArrayOf(
    '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007',
    '\u2008', '\u2009', '\u200A', '\u200B', '\u200C', '\u200D', '\u200E', '\u200F'
)

class SpaceDictionary : IDictionary {

    private var set = hashSetOf<String>()
    private var alphabet: AlphabetDictionary = AlphabetDictionary()


    override fun next(): String {
        val random = Random()
        val builder = StringBuilder(5)
        for (i in 0..5) {
            builder.append(character[random.nextInt(character.size)])
        }
        if (set.contains(builder.toString())) {
            return alphabet.next()
        }
        set.add(builder.toString())
        return builder.toString()
    }

    override fun reset() {
        /* no sense */
    }


}