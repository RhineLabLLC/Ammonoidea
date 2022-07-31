package rhinelab.ammonoidea.transformer.flow

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.Bootstrapper.debug
import rhinelab.ammonoidea.transformer.transformer
import rhinelab.ammonoidea.utils.VariableAllocator
import rhinelab.ammonoidea.utils.generateRandomString
import java.lang.reflect.Modifier
import java.security.SecureRandom
import kotlin.math.abs


val random = SecureRandom()

// skid util & self wrote
val switchMangler = transformer {
    val tmp = ArrayList<ClassNode>()
    classes.forEach { classNode ->
        classNode.methods.forEach processing@{ node ->
            if (Modifier.isAbstract(node.access) || Modifier.isNative(node.access)) return@processing

            val provider = VariableAllocator(node)
            val resultSlot: Int = provider.allocateVar()

            node.instructions.toArray().forEach { abstractInsnNode ->
                when (abstractInsnNode) {
                    is TableSwitchInsnNode -> {
                        val switchInsnNode: TableSwitchInsnNode = abstractInsnNode
                        val insnList = InsnList()
                        insnList.add(VarInsnNode(Opcodes.ISTORE, resultSlot))
                        (switchInsnNode.min..switchInsnNode.max).withIndex().forEach { (j, i) ->
                            insnList.add(VarInsnNode(Opcodes.ILOAD, resultSlot))
                            insnList.add(generateInsnList(i))
                            insnList.add(JumpInsnNode(Opcodes.IF_ICMPEQ, switchInsnNode.labels[j]))
                        }
                        insnList.add(JumpInsnNode(Opcodes.GOTO, switchInsnNode.dflt))
                        node.instructions.insert(abstractInsnNode, insnList)
                        node.instructions.remove(abstractInsnNode)
                    }

                    is LookupSwitchInsnNode -> {
                        val switchInsnNode: LookupSwitchInsnNode = abstractInsnNode
                        val insnList = InsnList()
                        insnList.add(VarInsnNode(Opcodes.ISTORE, resultSlot))
                        val keys: List<Int> = switchInsnNode.keys
                        keys.indices.forEach { i ->
                            insnList.add(VarInsnNode(Opcodes.ILOAD, resultSlot))
                            insnList.add(generateInsnList(i))
                            insnList.add(JumpInsnNode(Opcodes.IF_ICMPEQ, switchInsnNode.labels[i]))
                        }
                        insnList.add(JumpInsnNode(Opcodes.GOTO, switchInsnNode.dflt))
                        node.instructions.insert(abstractInsnNode, insnList)
                        node.instructions.remove(abstractInsnNode)
                    }
                }
            }
        }

        tmp.add(classNode)
    }
    classes = tmp
}

private fun generateInsnList(value: Int): InsnList {
    val methodInstructions = InsnList()
    var value = value

    if (value == 0) {
        val randomInt: Int = random.nextInt(100)
        methodInstructions.add(generateInsnList(randomInt))
        methodInstructions.add(generateInsnList(randomInt))
        methodInstructions.add(InsnNode(Opcodes.ICONST_M1))
        methodInstructions.add(InsnNode(Opcodes.IXOR))
        methodInstructions.add(InsnNode(Opcodes.IAND))
        return methodInstructions
    }
    val shiftOutput: IntArray = splitToLShift(value)

    if (shiftOutput[1] > 0) {
        methodInstructions.add(generateInsnList(shiftOutput[0]))
        methodInstructions.add(generateInsnList(shiftOutput[1]))
        methodInstructions.add(InsnNode(Opcodes.ISHL))
        return methodInstructions
    }
    val method: Int = if (abs(value) < 4) 0
        else if (abs(value) < Byte.MAX_VALUE) 1
        else {
            if (abs(value) > 0xFF) {
                3
            } else {
                2
            }
        }

    val negative: Boolean = value < 0

    value = abs(value)

    when (method) {
        0 -> {
            methodInstructions.add(LdcInsnNode(if (!debug) generateRandomString(value) else "0".repeat(value)))
            methodInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false))
        }

        1 -> {
            var A: Int = value
            val B: Int = random.nextInt(200)
            A = A xor B
            methodInstructions.add(generateIntPush(A))
            methodInstructions.add(generateIntPush(B))
            methodInstructions.add(InsnNode(Opcodes.IXOR))
        }

        2 -> {
            val ADD_1: Int = random.nextInt(value)
            val ADD_2: Int = random.nextInt(value)
            val ADD_3: Int = random.nextInt(value)
            val SUB: Int = ADD_1 + ADD_2 + ADD_3 - value
            methodInstructions.add(generateIntPush(ADD_1))
            methodInstructions.add(generateIntPush(ADD_2))
            methodInstructions.add(InsnNode(Opcodes.IADD))
            methodInstructions.add(generateIntPush(SUB))
            methodInstructions.add(InsnNode(Opcodes.ISUB))
            methodInstructions.add(generateIntPush(ADD_3))
            methodInstructions.add(InsnNode(Opcodes.IADD))
        }

        3 -> {
            val and: IntArray = splitToAnd(value)
            methodInstructions.add(generateIntPush(and[0]))
            methodInstructions.add(generateIntPush(and[1]))
            methodInstructions.add(InsnNode(Opcodes.IAND))
        }
    }
    if (negative) methodInstructions.add(InsnNode(Opcodes.INEG))

    return methodInstructions
}

private fun splitToAnd(number: Int): IntArray {
    val number2: Int = random.nextInt(Short.MAX_VALUE.toInt()) and number.inv()
    return intArrayOf(number2.inv(), number2 or number)
}

private fun splitToLShift(number: Int): IntArray {
    var number = number
    var shift = 0
    while (number.toLong() and 0x7ffffffffffffffEL.inv() == 0L && number != 0) {
        number = number shr 1
        shift++
    }
    return intArrayOf(number, shift)
}

private fun generateIntPush(i: Int): AbstractInsnNode? {
    if (i <= 5 && i >= -1) {
        return InsnNode(i + 3)
    }
    if (i >= -128 && i <= 127) {
        return IntInsnNode(Opcodes.BIPUSH, i)
    }
    return if (i >= -32768 && i <= 32767) {
        IntInsnNode(Opcodes.SIPUSH, i)
    } else LdcInsnNode(i)
}