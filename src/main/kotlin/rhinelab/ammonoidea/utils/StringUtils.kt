package rhinelab.ammonoidea.utils

import java.security.SecureRandom

fun generateRandomString(length: Int): String {
    val random = SecureRandom()
    val sb = StringBuilder(length)
    for (i in 0 until length) {
        sb.append(random.nextInt(0xFF).toChar())
    }
    return sb.toString()
}

fun generateRandomString(length: Int, chars: String): String {
    val random = java.util.Random()
    val sb = StringBuilder(length)
    for (i in 0 until length) {
        sb.append(chars[random.nextInt(chars.length)])
    }
    return sb.toString()
}