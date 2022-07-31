package rhinelab.ammonoidea.transformer.flow

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.Bootstrapper.debug
import rhinelab.ammonoidea.transformer.transformer
import rhinelab.ammonoidea.utils.INT
import rhinelab.ammonoidea.utils.generateRandomString
import rhinelab.ammonoidea.utils.randomInt
import java.lang.reflect.Modifier

// self wrote
val fakeGoto = transformer {
    // TODO("Check if field is necessary")
    val tmp = ArrayList<ClassNode>()
    classes.forEach { classNode ->
        println("Processing FakeGOTO...")
        val fieldName = if (!debug) generateRandomString(32) else generateRandomString(8, "ABCDEF1234567890")
        val fieldValue = randomInt()

        classNode.fields.add(
            FieldNode(
                ACC_PRIVATE or ACC_STATIC, fieldName, "I",
                null, fieldValue
            )
        )

        classNode.methods.forEach processing@{
            if (Modifier.isAbstract(it.access) || Modifier.isNative(it.access)) {
                return@processing
            }

            val instructions = it.instructions
            for (insn in instructions) {
                val opcode = insn.opcode
                if (opcode == GOTO) {

                    insn as JumpInsnNode

                    val jList = InsnList()
                    jList.add(FieldInsnNode(GETSTATIC, classNode.name, fieldName, INT))

                    val opt = if (fieldValue >= 0) IFGE else IFLT

                    jList.add(JumpInsnNode(opt, insn.label))
                    jList.add(InsnNode(ACONST_NULL))
                    jList.add(InsnNode(ATHROW))

                    it.instructions.insert(insn, jList)
                    it.instructions.remove(insn)
                }
            }
        }

        tmp.add(classNode)
    }

    classes = tmp
}