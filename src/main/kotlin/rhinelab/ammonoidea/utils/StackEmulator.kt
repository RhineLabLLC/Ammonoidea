package rhinelab.ammonoidea.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

/**
 * @param methodNode the method node we want to check.
 * @param breakPoint the opcode we want to break on.
 */
class StackHeightZeroFinder(methodNode: MethodNode, breakPoint: AbstractInsnNode) : Opcodes {
    /**
     * [MethodNode] we are checking.
     */
    private val methodNode: MethodNode

    /**
     * [AbstractInsnNode] opcode which is the breakpoint.
     */
    private val breakPoint: AbstractInsnNode

    /**
     * [HashSet] of [AbstractInsnNode]s where the stack is empty
     */
    private val emptyAt: MutableSet<AbstractInsnNode>

    init {
        this.methodNode = methodNode
        this.breakPoint = breakPoint
        emptyAt = HashSet()
    }

    /**
     * Returns [HashSet] of [AbstractInsnNode]s where the stack is empty.
     *
     * @return [HashSet] of [AbstractInsnNode]s where the stack is empty.
     */
    fun getEmptyAt(): Set<AbstractInsnNode> {
        return emptyAt
    }

    /**
     * Weakly emulates stack execution until no more instructions are left or the breakpoint is reached.
     */
    fun execute(debug: Boolean) {
        var stackSize = 0 // Emulated stack
        val excHandlers: MutableSet<LabelNode> = HashSet()
        methodNode.tryCatchBlocks.forEach { tryCatchBlockNode -> excHandlers.add(tryCatchBlockNode.handler) }
        for (i in 0 until methodNode.instructions.size()) {
            val insn: AbstractInsnNode = methodNode.instructions.get(i)
            if (insn is LabelNode && excHandlers.contains(insn)) stackSize =
                1 // Stack gets cleared and exception is pushed.
            if (stackSize < 0) throw IllegalStateException("Illegal stack size $stackSize")
            if (stackSize == 0) emptyAt.add(insn)
            if (breakPoint === insn) break
            when (insn.opcode) {
                ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
                FCONST_0, FCONST_1, FCONST_2, BIPUSH, SIPUSH, ILOAD, FLOAD, ALOAD,
                DUP, DUP_X1, DUP_X2, I2L, I2D, F2L, F2D, NEW ->                     // Pushes one-word constant to stack
                    stackSize++
                LDC -> {
                    val ldc: LdcInsnNode = insn as LdcInsnNode
                    if (ldc.cst is Long || ldc.cst is Double) stackSize++
                    stackSize++
                }
                LCONST_0, LCONST_1, DCONST_0, DCONST_1, LLOAD, DLOAD, DUP2, DUP2_X1, DUP2_X2 ->                     // Pushes two-word constant or two one-word constants to stack
                    stackSize += 2
                IALOAD, FALOAD, AALOAD, BALOAD, CALOAD, SALOAD, ISTORE, FSTORE, ASTORE, POP, IADD, FADD, ISUB, FSUB, IMUL, FMUL, IDIV, FDIV, IREM, FREM, ISHL, ISHR, IUSHR, LSHL, LSHR, LUSHR, IAND, IOR, IXOR, L2I, L2F, D2I, D2F, FCMPL, FCMPG, IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, TABLESWITCH, LOOKUPSWITCH, IRETURN, FRETURN, ATHROW, MONITORENTER, MONITOREXIT, IFNULL, IFNONNULL, ARETURN ->                     // Pops one-word constant off stack
                    stackSize--
                LSTORE, DSTORE, POP2, LADD, DADD, LSUB, DSUB, LMUL, DMUL, LDIV, DDIV, LREM, DREM, LAND, LOR, LXOR, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, LRETURN, DRETURN ->                     // Pops two-word or two one-word constant(s) off stack
                    stackSize -= 2
                IASTORE, FASTORE, AASTORE, BASTORE, CASTORE, SASTORE, LCMP, DCMPL, DCMPG ->                     // Pops three one-word constants off stack
                    stackSize -= 3
                LASTORE, DASTORE ->                     // Pops two one-word constants and one two-word constant off stack
                    stackSize -= 4
                GETSTATIC -> stackSize += doFieldEmulation((insn as FieldInsnNode).desc, true)
                PUTSTATIC -> stackSize += doFieldEmulation((insn as FieldInsnNode).desc, false)
                GETFIELD -> {
                    stackSize-- // Objectref
                    stackSize += doFieldEmulation((insn as FieldInsnNode).desc, true)
                }
                PUTFIELD -> {
                    stackSize-- // Objectref
                    stackSize += doFieldEmulation((insn as FieldInsnNode).desc, false)
                }
                INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE -> {
                    stackSize-- // Objectref
                    stackSize += doMethodEmulation((insn as MethodInsnNode).desc)
                }
                INVOKESTATIC -> stackSize += doMethodEmulation((insn as MethodInsnNode).desc)
                INVOKEDYNAMIC -> stackSize += doMethodEmulation((insn as InvokeDynamicInsnNode).desc)
                MULTIANEWARRAY -> {
                    stackSize -= (insn as MultiANewArrayInsnNode).dims
                    stackSize++ // arrayref
                }
                JSR, RET -> throw IllegalStateException("Unexpected instruction node.")
                else -> {}
            }
        }
    }

    companion object {
        private fun doFieldEmulation(desc: String, isGet: Boolean): Int {
            val type: Type = Type.getType(desc)
            val result = if (type.sort == Type.LONG || type.sort == Type.DOUBLE) 2 else 1
            return if (isGet) result else -result
        }

        private fun doMethodEmulation(desc: String): Int {
            var result = 0
            val args: Array<Type> = Type.getArgumentTypes(desc)
            val returnType: Type = Type.getReturnType(desc)
            for (type in args) {
                if (type.sort == Type.LONG || type.sort == Type.DOUBLE) result--
                result--
            }
            if (returnType.sort == Type.LONG || returnType.sort == Type.DOUBLE) result++
            if (returnType.sort != Type.VOID) result++
            return result
        }
    }
}