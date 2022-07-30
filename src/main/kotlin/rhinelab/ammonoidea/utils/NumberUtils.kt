package rhinelab.ammonoidea.utils

fun randomInt() = (Math.random() * Integer.MAX_VALUE).toInt()

fun randomInt(min: Int, max: Int): Int {
    return (min..max).random()
}