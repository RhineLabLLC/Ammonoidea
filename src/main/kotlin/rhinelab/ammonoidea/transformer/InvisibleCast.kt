package rhinelab.ammonoidea.transformer

import org.objectweb.asm.Opcodes

val invisibleCast = transformer {
    classes.forEach {
        it.methods.forEach { methodNode ->
            val insnList = methodNode.instructions
            val removeIndex = ArrayList<Int>()

            for ((index, value) in insnList.withIndex()) {
                if (value.opcode == Opcodes.CHECKCAST) {
                    insnList.remove(value)
                    removeIndex.add(index)
                }
            }
        }
    }
}