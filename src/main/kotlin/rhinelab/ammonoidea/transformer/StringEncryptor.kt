package rhinelab.ammonoidea.transformer

import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.Bootstrapper.debug
import rhinelab.ammonoidea.dictionary.UnicodeDictionary
import rhinelab.ammonoidea.isExcluded
import rhinelab.ammonoidea.utils.generateRandomString
import rhinelab.ammonoidea.utils.randomInt
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList


var hostClassName = ""
var keyVarName = if (!debug) UnicodeDictionary.next() else generateRandomString(12, "ABCDEF1234567890")

private var secretKey: SecretKeySpec? = null

val stringEncryptor = transformer {
    val tmp = ArrayList<ClassNode>()

    var hostClass = classes.random()
    var whileCount = 0
    while (
        hostClass.superName != "java/lang/Object" ||
        hostClass.access != Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER ||
        hostClass.version < Opcodes.V1_7
    ) {
        hostClass = classes.random()
        whileCount++
        if (whileCount >= 5) {
            return@transformer
        }
    }

    hostClassName = hostClass.name

    val keySetterName =
        if (debug) "String_KeySetter_" + generateRandomString(4, "ABCDEF1234567890") else generateRandomString(
            8,
            "菊石混淆器=祝您新年快乐阖家幸福|事业蒸蒸日上?生活丰富美满|全家trans\""
        )

    val decryptorName =
        if (debug) "String_Decrypt_" + generateRandomString(4, "ABCDEF1234567890") else generateRandomString(
            12,
            "菊石混淆器=祝您新年快乐阖家幸福|事业蒸蒸日上?生活丰富美满|全家trans\""
        )

    classes.forEach { classNode ->
        if (isExcluded(classNode)) {
            tmp.add(classNode)
            return@forEach
        }

        classNode.methods
            .filter { it.instructions != null }
            .forEach {
                val insnList = it.instructions
                for (i in insnList) {
                    if (i is LdcInsnNode && i.cst is String) {
                        val origin = i.cst as String
                        val key = generateRandomString(12)
                        val encrypted = encrypt(origin, key)

                        val decList = InsnList()
                        decList.add(LdcInsnNode(encrypted))
                        decList.add(LdcInsnNode(key))
                        decList.add(
                            MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                hostClassName,
                                decryptorName,
                                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
                            )
                        )

                        insnList.insert(i, decList)
                        insnList.remove(i)
                    }
                }

                it.instructions = insnList
            }

        tmp.add(classNode)
    }

    hostClass.methods.add(generateKeySetter(keySetterName))
    hostClass.methods.add(generateDecryptMethod(decryptorName, keySetterName))

    hostClass.fields.add(
        FieldNode(
            Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, keyVarName,
            "Ljavax/crypto/spec/SecretKeySpec;", null, null
        )
    )

    classes = tmp
}

fun setKey(myKey: String) {
    val sha: MessageDigest
    try {
        var key: ByteArray? = myKey.toByteArray(StandardCharsets.UTF_8)
        sha = MessageDigest.getInstance("SHA-1")
        key = sha.digest(key)
        key = key?.copyOf(16)
        secretKey = SecretKeySpec(key, "AES")
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
}

fun encrypt(strToEncrypt: String, secret: String): String? {
    try {
        setKey(secret)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.getEncoder()
            .encodeToString(cipher.doFinal(strToEncrypt.toByteArray(StandardCharsets.UTF_8)))
    } catch (e: Exception) {
        println("Error while encrypting: $e")
    }
    return null
}

fun generateKeySetter(name: String): MethodNode {
    val method = MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, name, "(Ljava/lang/String;)V", null, null)

    val list = InsnList()
    val exStart = LabelNode()
    val exEnd = LabelNode()
    val exHandler = LabelNode()
    val labelReturn = LabelNode()

    method.tryCatchBlocks.add(TryCatchBlockNode(exStart, exEnd, exHandler, "java/security/NoSuchAlgorithmException"))
    list.add(exStart)
    list.add(VarInsnNode(Opcodes.ALOAD, 0))
    // GETSTATIC java/nio/charset/StandardCharsets.UTF_8 Ljava/nio/charset/Charset;
    list.add(
        FieldInsnNode(
            Opcodes.GETSTATIC,
            "java/nio/charset/StandardCharsets",
            "UTF_8",
            "Ljava/nio/charset/Charset;"
        )
    )
    list.add(
        MethodInsnNode(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/String",
            "getBytes",
            "(Ljava/nio/charset/Charset;)[B"
        )
    )
    list.add(VarInsnNode(Opcodes.ASTORE, 2))
    list.add(LabelNode())
    list.add(LdcInsnNode("SHA-1"))
    list.add(
        MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "java/security/MessageDigest",
            "getInstance",
            "(Ljava/lang/String;)Ljava/security/MessageDigest;"
        )
    )
    list.add(VarInsnNode(Opcodes.ASTORE, 1))
    list.add(VarInsnNode(Opcodes.ALOAD, 1))
    list.add(VarInsnNode(Opcodes.ALOAD, 2))
    list.add(
        MethodInsnNode(
            Opcodes.INVOKEVIRTUAL,
            "java/security/MessageDigest",
            "digest",
            "([B)[B"
        )
    )
    list.add(VarInsnNode(Opcodes.ASTORE, 2))
    list.add(VarInsnNode(Opcodes.ALOAD, 2))
    list.add(IntInsnNode(Opcodes.BIPUSH, 16))
    list.add(
        MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "java/util/Arrays",
            "copyOf",
            "([BI)[B"
        )
    )
    list.add(VarInsnNode(Opcodes.ASTORE, 2))
    list.add(TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/SecretKeySpec"))
    list.add(InsnNode(Opcodes.DUP))
    list.add(VarInsnNode(Opcodes.ALOAD, 2))
    list.add(LdcInsnNode("AES"))
    list.add(
        MethodInsnNode(
            Opcodes.INVOKESPECIAL,
            "javax/crypto/spec/SecretKeySpec",
            "<init>",
            "([BLjava/lang/String;)V"
        )
    )
    list.add(FieldInsnNode(Opcodes.PUTSTATIC, hostClassName, keyVarName, "Ljavax/crypto/spec/SecretKeySpec;"))
    list.add(exEnd)
    list.add(JumpInsnNode(Opcodes.GOTO, labelReturn))
    list.add(exHandler)
    list.add(VarInsnNode(Opcodes.ASTORE, 2))
    list.add(VarInsnNode(Opcodes.ALOAD, 2))
    list.add(
        MethodInsnNode(
            Opcodes.INVOKEVIRTUAL,
            "java/security/NoSuchAlgorithmException",
            "printStackTrace",
            "()V"
        )
    )
    list.add(labelReturn)
    list.add(InsnNode(Opcodes.RETURN))

    method.instructions = list
    return method
}

fun generateDecryptMethod(name: String, keySetterName: String): MethodNode {
    val method = MethodNode(
        Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
        name,
        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
        null,
        null
    )

    val list = InsnList()
    val exStart = LabelNode()
    val exEnd = LabelNode()
    val exHandler = LabelNode()

    method.tryCatchBlocks.add(TryCatchBlockNode(exStart, exEnd, exHandler, "java/lang/Exception"))
    list.add(exStart)
    list.add(VarInsnNode(Opcodes.ALOAD, 1))
    list.add(MethodInsnNode(Opcodes.INVOKESTATIC, hostClassName, keySetterName, "(Ljava/lang/String;)V"))
    list.add(LdcInsnNode("AES/ECB/PKCS5PADDING"))
    list.add(
        MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "javax/crypto/Cipher",
            "getInstance",
            "(Ljava/lang/String;)Ljavax/crypto/Cipher;"
        )
    )
    list.add(VarInsnNode(Opcodes.ASTORE, 2))
    list.add(VarInsnNode(Opcodes.ALOAD, 2))
    list.add(InsnNode(Opcodes.ICONST_2))
    list.add(FieldInsnNode(Opcodes.GETSTATIC, hostClassName, keyVarName, "Ljavax/crypto/spec/SecretKeySpec;"))
    list.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;)V"))
    list.add(TypeInsnNode(Opcodes.NEW, "java/lang/String"))
    list.add(InsnNode(Opcodes.DUP))
    list.add(VarInsnNode(Opcodes.ALOAD, 2))
    list.add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64\$Decoder;"))
    list.add(VarInsnNode(Opcodes.ALOAD, 0))
    list.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Base64\$Decoder", "decode", "(Ljava/lang/String;)[B"))
    list.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B"))
    list.add(
        FieldInsnNode(
            Opcodes.GETSTATIC,
            "java/nio/charset/StandardCharsets",
            "UTF_8",
            "Ljava/nio/charset/Charset;"
        )
    )
    list.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V"))
    list.add(exEnd)
    list.add(InsnNode(Opcodes.ARETURN))
    list.add(exHandler)
    list.add(VarInsnNode(Opcodes.ASTORE, 2))
    list.add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
    list.add(VarInsnNode(Opcodes.ALOAD, 2))
    list.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Exception", "toString", "()Ljava/lang/String;"))
    val handle = Handle(
        Opcodes.H_INVOKESTATIC,
        "java/lang/invoke/StringConcatFactory",
        "makeConcatWithConstants",
        "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
        false
    )
    list.add(
        InvokeDynamicInsnNode(
            "makeConcatWithConstants",
            "(Ljava/lang/String;)Ljava/lang/String;",
            handle,
            "Error while decrypting: \u0001"
        )
    )
    list.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"))
    list.add(InsnNode(Opcodes.ACONST_NULL))
    list.add(InsnNode(Opcodes.ARETURN))

    method.instructions = list
    return method
}