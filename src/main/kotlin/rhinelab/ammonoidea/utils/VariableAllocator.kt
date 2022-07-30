package rhinelab.ammonoidea.utils

import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.Opcodes.*;
import java.lang.reflect.Modifier


class VariableAllocator private constructor() {
    private var max = 0
    private var argumentSize = 0

    constructor(method: MethodNode) : this() {
        if (!Modifier.isStatic(method.access)) registerExisting(0, Type.getType("Ljava/lang/Object;"))
        for (argumentType in Type.getArgumentTypes(method.desc)) {
            registerExisting(argumentType.size + max - 1, argumentType)
        }
        argumentSize = max
        for (abstractInsnNode in method.instructions.toArray()) {
            if (abstractInsnNode is VarInsnNode) {
                getType(abstractInsnNode)?.let {
                    registerExisting(
                        abstractInsnNode.`var`,
                        it
                    )
                }
            }
        }
    }

    private fun registerExisting(`var`: Int, type: Type) {
        if (`var` >= max) max = `var` + type.size
    }

    fun isUnallocated(`var`: Int): Boolean {
        return `var` >= max
    }

    fun isArgument(`var`: Int): Boolean {
        return `var` < argumentSize
    }

    fun allocateVar(): Int {
        return max++
    }
}

private fun getType(insn: VarInsnNode): Type? {

    val offset: Int = when (insn.opcode) {
            in ISTORE..ASTORE -> insn.opcode - ISTORE
            in ILOAD..ALOAD -> insn.opcode - ILOAD
            RET -> throw UnsupportedOperationException(
                "RET is not supported"
            )

            else -> throw UnsupportedOperationException()
        }

    when (offset) {
        0 -> return Type.INT_TYPE
        LLOAD - ILOAD -> return Type.LONG_TYPE
        FLOAD - ILOAD -> return Type.FLOAT_TYPE
        DLOAD - ILOAD -> return Type.DOUBLE_TYPE
        ALOAD - ILOAD -> return Type.getType("Ljava/lang/Object;")
    }

    throw IllegalStateException("I dunno this situation")
}