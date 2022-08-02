package rhinelab.ammonoidea.transformer

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.isExcluded

// self wrote
val invisibleCast = transformer {
    val tmp = ArrayList<ClassNode>()
    classes.forEach {
        if (isExcluded(it)) {
            tmp.add(it)
            return@forEach
        }
        it.methods.forEach { methodNode ->
            val insnList = methodNode.instructions
            val removeIndex = ArrayList<Int>()

            for ((index, value) in insnList.withIndex()) {
                if (value.opcode == Opcodes.CHECKCAST) {
                    insnList.remove(value)
                    removeIndex.add(index)
                }
            }

            methodNode.instructions = insnList
        }
        tmp.add(it)
    }

    classes = tmp
}