package rhinelab.ammonoidea.utils

fun randomInt() = (Math.random() * Integer.MAX_VALUE).toInt()

fun randomInt(min: Int, max: Int): Int {
    return (min..max).random()
}

fun randomLong() = (Math.random() * Long.MAX_VALUE).toLong()

fun randomLong(min: Long, max: Long): Long {
    return (min..max).random()
}