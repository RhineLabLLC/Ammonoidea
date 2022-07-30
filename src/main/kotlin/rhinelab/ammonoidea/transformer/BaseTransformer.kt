package rhinelab.ammonoidea.transformer

import org.objectweb.asm.tree.ClassNode
import kotlin.random.Random

abstract class BaseTransformer {
    lateinit var classes: ArrayList<ClassNode>
    abstract fun transform()
}

fun transformer(block: BaseTransformer.() -> Unit): BaseTransformer {
    val transformer = object : BaseTransformer() {
        override fun transform() {
            block()
        }
    }
    transformer.classes = ArrayList()
    return transformer
}