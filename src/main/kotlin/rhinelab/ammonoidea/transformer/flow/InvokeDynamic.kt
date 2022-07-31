package rhinelab.ammonoidea.transformer.flow

import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.Bootstrapper.debug
import rhinelab.ammonoidea.transformer.transformer
import rhinelab.ammonoidea.utils.generateRandomString
import java.util.concurrent.atomic.AtomicInteger

var key1 = 0
var key2 = 0
var key3 = 0

val invokeDynamic = transformer {
    val tmp = ArrayList<ClassNode>()
    val counter = AtomicInteger()
    var hostClass = classes.random()
    var whileCount = 0

    key1 = if (debug) 0 else 0xCAFE
    key2 = if (debug) 0 else 0xBEEF
    key3 = if (debug) 0 else 0xDEAD

    while (hostClass.superName != "java/lang/Object" || hostClass.access != ACC_PUBLIC or ACC_SUPER || hostClass.version < V1_7) {
        hostClass = classes.random()
        whileCount++
        if (whileCount >= 5) {
            return@transformer
        }
    }
    var bsmName = "InvokeDynamic_Host_" + generateRandomString(4, "ABCDEF1234567890")
    if (!debug) {
        bsmName = generateRandomString(
        16,
        "%为国家哪何曾半日闲空 我也曾征过了塞北西东\"官封到节度使皇王恩重{霎时间身不爽瞌睡朦胧*"
        )
    }
    hostClass.methods.add(createBootstrap(bsmName, hostClass.name))
    val bsmHandle = Handle(
        H_INVOKESTATIC, hostClass.name, bsmName,
        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;" +
                "Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false
    )
    classes.stream().forEach processClass@{ classNode ->
        if (classNode.superName == "java/lang/Enum" || classNode.version < V1_7) {
            tmp.add(classNode)
            return@processClass
        }
        classNode.methods.stream().filter { methodNode ->
            methodNode.instructions != null && methodNode.instructions.size() > 0
        }.forEach { methodNode ->
            for (insn in methodNode.instructions) {
                if (insn is MethodInsnNode && insn.opcode != INVOKESPECIAL) {
                    val methodInsnNode: MethodInsnNode = insn
                    val isStatic = methodInsnNode.opcode == INVOKESTATIC

                    val oldDesc = methodInsnNode.desc

                    val newSig = if (isStatic) oldDesc else oldDesc.replace("(", "(Ljava/lang/Object;")

                    val returnType: Type = Type.getReturnType(methodInsnNode.desc)
                    val opcode = if (isStatic) 0 else 1
                    val idyInsn = InvokeDynamicInsnNode(
                        if (!debug) generateRandomString(
                            4,
                            "菊石混淆器=祝您新年快乐阖家幸福|事业蒸蒸日上?生活丰富美满|全家trans\""
                        )
                        else "InvokeDynamic_" + generateRandomString(4, "ABCDEF1234567890"),
                        newSig,
                        bsmHandle,
                        opcode,
                        encrypt(methodInsnNode.owner.replace("/", "."), key1),
                        encrypt(methodInsnNode.name, key2),
                        encrypt(methodInsnNode.desc, key3)
                    )
                    methodNode.instructions.set(insn, idyInsn)
                    if (returnType.sort == Type.ARRAY) methodNode.instructions.insert(
                        idyInsn, TypeInsnNode(
                            CHECKCAST,
                            returnType.internalName
                        )
                    )
                    counter.incrementAndGet()
                }
            }
        }
        tmp.add(classNode)
    }
    classes = tmp
}

private fun encrypt(msg: String, key: Int): String {
    val encClassNameChars = msg.toCharArray()
    val classNameChars = CharArray(encClassNameChars.size)
    for (i in encClassNameChars.indices) classNameChars[i] = (encClassNameChars[i].code xor key).toChar()
    return String(classNameChars)
}

private fun createBootstrap(bsmName: String, className: String): MethodNode {
    val mv = MethodNode(
        ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC + ACC_BRIDGE, bsmName,
        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;" +
                "Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null
    )
    mv.visitCode()
    val l0 = Label()
    val l1 = Label()
    val l2 = Label()
    mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable")
    val l3 = Label()
    val l4 = Label()
    val l5 = Label()
    mv.visitTryCatchBlock(l3, l4, l5, "java/lang/Exception")
    mv.visitLabel(l3)
    mv.visitVarInsn(ALOAD, 4)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false)
    mv.visitVarInsn(ASTORE, 7)
    val l6 = Label()
    mv.visitLabel(l6)
    mv.visitVarInsn(ALOAD, 7)
    mv.visitInsn(ARRAYLENGTH)
    mv.visitIntInsn(NEWARRAY, T_CHAR)
    mv.visitVarInsn(ASTORE, 8)
    val l7 = Label()
    mv.visitLabel(l7)
    mv.visitInsn(ICONST_0)
    mv.visitVarInsn(ISTORE, 9)
    val l8 = Label()
    mv.visitLabel(l8)
    val l9 = Label()
    mv.visitJumpInsn(GOTO, l9)
    val l10 = Label()
    mv.visitLabel(l10)
    mv.visitFrame(F_APPEND, 3, arrayOf<Any>("[C", "[C", INTEGER), 0, null)
    mv.visitVarInsn(ALOAD, 8)
    mv.visitVarInsn(ILOAD, 9)
    mv.visitVarInsn(ALOAD, 7)
    mv.visitVarInsn(ILOAD, 9)
    mv.visitInsn(CALOAD)
    mv.visitIntInsn(SIPUSH, key1)
    mv.visitInsn(IXOR)
    mv.visitInsn(I2C)
    mv.visitInsn(CASTORE)
    val l11 = Label()
    mv.visitLabel(l11)
    mv.visitIincInsn(9, 1)
    mv.visitLabel(l9)
    mv.visitFrame(F_SAME, 0, null, 0, null)
    mv.visitVarInsn(ILOAD, 9)
    mv.visitVarInsn(ALOAD, 7)
    mv.visitInsn(ARRAYLENGTH)
    mv.visitJumpInsn(IF_ICMPLT, l10)
    val l12 = Label()
    mv.visitLabel(l12)
    mv.visitVarInsn(ALOAD, 5)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false)
    mv.visitVarInsn(ASTORE, 9)
    val l13 = Label()
    mv.visitLabel(l13)
    mv.visitVarInsn(ALOAD, 9)
    mv.visitInsn(ARRAYLENGTH)
    mv.visitIntInsn(NEWARRAY, T_CHAR)
    mv.visitVarInsn(ASTORE, 10)
    val l14 = Label()
    mv.visitLabel(l14)
    mv.visitInsn(ICONST_0)
    mv.visitVarInsn(ISTORE, 11)
    val l15 = Label()
    mv.visitLabel(l15)
    val l16 = Label()
    mv.visitJumpInsn(GOTO, l16)
    val l17 = Label()
    mv.visitLabel(l17)
    mv.visitFrame(
        F_FULL, 12, arrayOf<Any>(
            "java/lang/Object", "java/lang/Object", "java/lang/Object",
            "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C",
            INTEGER
        ), 0, arrayOf()
    )
    mv.visitVarInsn(ALOAD, 10)
    mv.visitVarInsn(ILOAD, 11)
    mv.visitVarInsn(ALOAD, 9)
    mv.visitVarInsn(ILOAD, 11)
    mv.visitInsn(CALOAD)
    mv.visitIntInsn(SIPUSH, key2)
    mv.visitInsn(IXOR)
    mv.visitInsn(I2C)
    mv.visitInsn(CASTORE)
    val l18 = Label()
    mv.visitLabel(l18)
    mv.visitIincInsn(11, 1)
    mv.visitLabel(l16)
    mv.visitFrame(F_SAME, 0, null, 0, null)
    mv.visitVarInsn(ILOAD, 11)
    mv.visitVarInsn(ALOAD, 9)
    mv.visitInsn(ARRAYLENGTH)
    mv.visitJumpInsn(IF_ICMPLT, l17)
    val l19 = Label()
    mv.visitLabel(l19)
    mv.visitVarInsn(ALOAD, 6)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false)
    mv.visitVarInsn(ASTORE, 11)
    val l20 = Label()
    mv.visitLabel(l20)
    mv.visitVarInsn(ALOAD, 11)
    mv.visitInsn(ARRAYLENGTH)
    mv.visitIntInsn(NEWARRAY, T_CHAR)
    mv.visitVarInsn(ASTORE, 12)
    val l21 = Label()
    mv.visitLabel(l21)
    mv.visitInsn(ICONST_0)
    mv.visitVarInsn(ISTORE, 13)
    val l22 = Label()
    mv.visitLabel(l22)
    val l23 = Label()
    mv.visitJumpInsn(GOTO, l23)
    val l24 = Label()
    mv.visitLabel(l24)
    mv.visitFrame(
        F_FULL, 14, arrayOf<Any>(
            "java/lang/Object", "java/lang/Object", "java/lang/Object",
            "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C",
            "[C", "[C", INTEGER
        ), 0, arrayOf()
    )
    mv.visitVarInsn(ALOAD, 12)
    mv.visitVarInsn(ILOAD, 13)
    mv.visitVarInsn(ALOAD, 11)
    mv.visitVarInsn(ILOAD, 13)
    mv.visitInsn(CALOAD)
    mv.visitIntInsn(SIPUSH, key3)
    mv.visitInsn(IXOR)
    mv.visitInsn(I2C)
    mv.visitInsn(CASTORE)
    val l25 = Label()
    mv.visitLabel(l25)
    mv.visitIincInsn(13, 1)
    mv.visitLabel(l23)
    mv.visitFrame(F_SAME, 0, null, 0, null)
    mv.visitVarInsn(ILOAD, 13)
    mv.visitVarInsn(ALOAD, 11)
    mv.visitInsn(ARRAYLENGTH)
    mv.visitJumpInsn(IF_ICMPLT, l24)
    val l26 = Label()
    mv.visitLabel(l26)
    mv.visitVarInsn(ALOAD, 3)
    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer")
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
    mv.visitVarInsn(ISTORE, 14)
    val l27 = Label()
    mv.visitLabel(l27)
    mv.visitVarInsn(ILOAD, 14)
    mv.visitIntInsn(SIPUSH, 256)
    mv.visitInsn(ISHL)
    mv.visitIntInsn(SIPUSH, 255)
    mv.visitInsn(IAND)
    mv.visitVarInsn(ISTORE, 14)
    val l28 = Label()
    mv.visitLabel(l28)
    mv.visitVarInsn(ILOAD, 14)
    val l29 = Label()
    val l30 = Label()
    val l31 = Label()
    mv.visitTableSwitchInsn(0, 1, l31, l29, l30)
    mv.visitLabel(l29)
    mv.visitFrame(
        F_FULL, 15, arrayOf<Any>(
            "java/lang/Object", "java/lang/Object", "java/lang/Object",
            "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C",
            "[C", "[C", TOP, INTEGER
        ), 0, arrayOf()
    )
    mv.visitVarInsn(ALOAD, 0)
    mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandles\$Lookup")
    mv.visitTypeInsn(NEW, "java/lang/String")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 8)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false)
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false)
    mv.visitTypeInsn(NEW, "java/lang/String")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 10)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false)
    mv.visitTypeInsn(NEW, "java/lang/String")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 12)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false)
    mv.visitLdcInsn(Type.getType("L$className;"))
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false)
    mv.visitMethodInsn(
        INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString",
        "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false
    )
    mv.visitMethodInsn(
        INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findStatic",
        "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
        false
    )
    mv.visitVarInsn(ASTORE, 13)
    val l32 = Label()
    mv.visitLabel(l32)
    val l33 = Label()
    mv.visitJumpInsn(GOTO, l33)
    mv.visitLabel(l30)
    mv.visitFrame(F_SAME, 0, null, 0, null)
    mv.visitVarInsn(ALOAD, 0)
    mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandles\$Lookup")
    mv.visitTypeInsn(NEW, "java/lang/String")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 8)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false)
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false)
    mv.visitTypeInsn(NEW, "java/lang/String")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 10)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false)
    mv.visitTypeInsn(NEW, "java/lang/String")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 12)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false)
    mv.visitLdcInsn(Type.getType("L$className;"))
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false)
    mv.visitMethodInsn(
        INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString",
        "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false
    )
    mv.visitMethodInsn(
        INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findVirtual",
        "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
        false
    )
    mv.visitVarInsn(ASTORE, 13)
    val l34 = Label()
    mv.visitLabel(l34)
    mv.visitJumpInsn(GOTO, l33)
    mv.visitLabel(l31)
    mv.visitFrame(F_SAME, 0, null, 0, null)
    mv.visitTypeInsn(NEW, "java/lang/BootstrapMethodError")
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/BootstrapMethodError", "<init>", "()V", false)
    mv.visitInsn(ATHROW)
    mv.visitLabel(l33)
    mv.visitFrame(
        F_FULL, 15, arrayOf<Any>(
            "java/lang/Object", "java/lang/Object", "java/lang/Object",
            "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C",
            "[C", "[C", "java/lang/invoke/MethodHandle", INTEGER
        ), 0, arrayOf()
    )
    mv.visitVarInsn(ALOAD, 13)
    mv.visitVarInsn(ALOAD, 2)
    mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodType")
    mv.visitMethodInsn(
        INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType",
        "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false
    )
    mv.visitVarInsn(ASTORE, 13)
    mv.visitLabel(l0)
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false)
    mv.visitMethodInsn(
        INVOKESTATIC, "java/util/concurrent/ThreadLocalRandom", "current",
        "()Ljava/util/concurrent/ThreadLocalRandom;", false
    )
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ThreadLocalRandom", "nextInt", "()I", false)
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "(Ljava/lang/String;)Ljava/lang/Process;", false)
    mv.visitInsn(POP)
    mv.visitLabel(l1)
    val l35 = Label()
    mv.visitJumpInsn(GOTO, l35)
    mv.visitLabel(l2)
    mv.visitFrame(F_SAME1, 0, null, 1, arrayOf<Any>("java/lang/Throwable"))
    mv.visitVarInsn(ASTORE, 15)
    mv.visitLabel(l35)
    mv.visitFrame(F_SAME, 0, null, 0, null)
    mv.visitTypeInsn(NEW, "java/lang/invoke/ConstantCallSite")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 13)
    mv.visitMethodInsn(
        INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>",
        "(Ljava/lang/invoke/MethodHandle;)V", false
    )
    mv.visitLabel(l4)
    mv.visitInsn(ARETURN)
    mv.visitLabel(l5)
    mv.visitFrame(
        F_FULL, 7, arrayOf<Any>(
            "java/lang/Object", "java/lang/Object", "java/lang/Object",
            "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object"
        ),
        1, arrayOf<Any>("java/lang/Exception")
    )
    mv.visitVarInsn(ASTORE, 7)
    val l36 = Label()
    mv.visitLabel(l36)
    mv.visitVarInsn(ALOAD, 7)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false)
    val l37 = Label()
    mv.visitLabel(l37)
    mv.visitTypeInsn(NEW, "java/lang/BootstrapMethodError")
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/BootstrapMethodError", "<init>", "()V", false)
    mv.visitInsn(ATHROW)
    val l38 = Label()
    mv.visitLabel(l38)
    mv.visitMaxs(6, 16)
    mv.visitEnd()
    return mv
}