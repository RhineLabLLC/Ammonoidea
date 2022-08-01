package rhinelab.ammonoidea.transformer

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.utils.randomInt
import rhinelab.ammonoidea.utils.randomLong

fun isIntInsn(insn: AbstractInsnNode?): Boolean {
    if (insn == null) return false
    val op = insn.opcode
    return ((op >= Opcodes.ICONST_M1) && (op <= Opcodes.ICONST_5)) ||
            (op == Opcodes.BIPUSH) || (op == Opcodes.SIPUSH) ||
            ((insn is LdcInsnNode) && (insn.cst is Int))
}

fun getIntFromInsn(insn: AbstractInsnNode): Int {
    val opcode = insn.opcode

    if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) return opcode - 3
    if (insn is IntInsnNode && insn.opcode != Opcodes.NEWARRAY) return insn.operand
    if (insn is LdcInsnNode && insn.cst is Int) return insn.cst as Int

    throw IllegalStateException("Not expecting non-int int instruction")
}

fun modifyInt(num: Int): InsnList {
    var cur = randomInt(16, num)
    val insnList = InsnList()

    insnList.add(LdcInsnNode(cur))

    val opr = randomInt(0, cur)
    insnList.add(LdcInsnNode(opr))

    when (randomInt(0, 3)) {
        0 -> {
            insnList.add(InsnNode(Opcodes.IOR))
            cur = cur or opr
        }

        1 -> {
            insnList.add(InsnNode(Opcodes.IXOR))
            cur = cur xor opr
        }

        2 -> {
            insnList.add((InsnNode(Opcodes.IAND)))
            cur = cur and opr
        }

        3 -> {
            if (opr in 1..4) {
                insnList.add(InsnNode(Opcodes.ISHR))
                cur = cur shr opr
            }
        }
    }

    val n = num xor cur
    insnList.add(LdcInsnNode(n))
    insnList.add(InsnNode(Opcodes.IXOR))

    return insnList
}

val numberBitwise = transformer {
    classes.forEach classProcess@{
        it.methods.stream().filter { it.instructions != null }.forEach { mn ->
            val insnList = mn.instructions

            insnList.forEach insnProcess@{insn ->
                if (isIntInsn(insn)) {
                    val replacement = modifyInt(getIntFromInsn(insn))
                    insnList.insert(insn, replacement)
                    insnList.remove(insn)
                }
            }
        }
    }
}