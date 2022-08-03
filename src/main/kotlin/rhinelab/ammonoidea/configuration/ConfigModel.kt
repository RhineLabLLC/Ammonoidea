package rhinelab.ammonoidea.configuration

data class ConfigModel(
    val input: String,
    val output: String,
    var isDebug: Boolean = false,
    var exclusion: ArrayList<String> = ArrayList()
)