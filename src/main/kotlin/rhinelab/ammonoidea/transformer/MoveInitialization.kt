package rhinelab.ammonoidea.transformer

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodNode
import rhinelab.ammonoidea.Bootstrapper.classes
import java.lang.reflect.Modifier

val moveInitialization = transformer {
    val tmp = ArrayList<ClassNode>()
    classes.forEach {
        it.fields.forEach fieldProcess@{ field ->
            val value = field.value ?: return@fieldProcess

            if (Modifier.isStatic(field.access)) {
                val clinit = getOrCreateClinit(it)
                val list = InsnList()

                if (value is Number || value is String) {
                    list.add(LdcInsnNode(value))
                    list.add(FieldInsnNode(Opcodes.PUTSTATIC, it.name, field.name, field.desc))
                }

                clinit.instructions.add(list)
            }
        }
        tmp.add(it)
    }
    classes = tmp
}

fun getOrCreateClinit(classNode: ClassNode): MethodNode {
    var clinit: MethodNode? = classNode.methods.stream().filter { methodNode ->
        (methodNode.name == "<clinit>" && methodNode.desc == "()V")
    }.findAny().orElse(null)
    if (clinit == null) {
        clinit = MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
        clinit.instructions.add(InsnNode(Opcodes.RETURN))
        classNode.methods.add(clinit)
    }
    return clinit
}