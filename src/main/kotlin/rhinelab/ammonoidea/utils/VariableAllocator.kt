package rhinelab.ammonoidea.utils

import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.Opcodes.*;
import java.lang.reflect.Modifier


class VariableAllocator(method: MethodNode) {
    private var max = 0
    private var argumentSize = 0

    init {
        if (!Modifier.isStatic(method.access)) registerExisting(0, Type.getType("Ljava/lang/Object;"))
        for (argumentType in Type.getArgumentTypes(method.desc)) {
            registerExisting(argumentType.size + max - 1, argumentType)
        }
        argumentSize = max

        for (abstractInsnNode in method.instructions.toArray()) {
            if (abstractInsnNode is VarInsnNode) {
                registerExisting(abstractInsnNode.`var`, getType(abstractInsnNode))
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

private val objectType = Type.getObjectType("java/lang/Object")

private fun getType(insn: VarInsnNode): Type {
    return when (insn.opcode) {
        ILOAD, ISTORE -> Type.INT_TYPE
        LLOAD, LSTORE -> Type.LONG_TYPE
        FLOAD, FSTORE -> Type.FLOAT_TYPE
        DLOAD, DSTORE -> Type.DOUBLE_TYPE
        ALOAD, ASTORE -> objectType

        else -> throw IllegalArgumentException("Unknown variable type: ${insn.opcode}")
    }
}