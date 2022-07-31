package rhinelab.ammonoidea.transformer


abstract class BaseTransformer {
    abstract fun transform()
}

fun transformer(block: BaseTransformer.() -> Unit) = object : BaseTransformer() {
    override fun transform() {
        block()
    }
}