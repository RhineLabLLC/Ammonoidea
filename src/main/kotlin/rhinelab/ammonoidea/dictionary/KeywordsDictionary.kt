package rhinelab.ammonoidea.dictionary

private val JAVA_KEYWORDS = arrayOf(
    "abstract",
    "assert",
    "boolean",
    "break",
    "byte",
    "case",
    "catch",
    "char",
    "class",
    "const",
    "continue",
    "default",
    "do",
    "double",
    "else",
    "enum",
    "extends",
    "false",
    "final",
    "finally",
    "float",
    "for",
    "goto",
    "if",
    "implements",
    "import",
    "instanceof",
    "int",
    "interface",
    "long",
    "native",
    "new",
    "null",
    "package",
    "private",
    "protected",
    "public",
    "return",
    "short",
    "static",
    "strictfp",
    "super",
    "switch",
    "synchronized",
    "this",
    "throw",
    "throws",
    "transient",
    "true",
    "try",
    "void",
    "volatile",
    "while"
)


class KeywordsDictionary : IDictionary {
    private var index = 0
    private var loop = 0
    override fun next(): String {
        return if (JAVA_KEYWORDS.size != index) {
            JAVA_KEYWORDS[index++]
        } else {
            var loopStr = loop++.toString()
            while (loopStr.length < 4) loopStr = "0$loopStr"
            JAVA_KEYWORDS[(0..JAVA_KEYWORDS.size).random()] + "_" + loopStr
        }
    }

    override fun reset() {
        index = 0
        loop = 0
    }


}