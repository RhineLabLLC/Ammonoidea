package rhinelab.ammonoidea

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.Bootstrapper.resources
import rhinelab.ammonoidea.transformer.flow.fakeGoto
import rhinelab.ammonoidea.transformer.flow.invokeDynamic
import rhinelab.ammonoidea.transformer.flow.stupidTransformer
import rhinelab.ammonoidea.transformer.flow.switchMangler
import rhinelab.ammonoidea.transformer.numberBitwise
import rhinelab.ammonoidea.transformer.stringEncryptor
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

object Bootstrapper {
    var classes = ArrayList<ClassNode>()
    val resources = HashMap<String, ByteArray>()
    var debug = false
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: ammonoidea.jar <Input Jar Path> <Output Jar path>")
        return
    }
    val inJar = File(args[0])
    val outJar = File(args[1])
    if (!inJar.exists()) {
        println("Error: Input jar does not exist")
        return
    }
    outJar.createNewFile()
    process(inJar, outJar)
    // processSingleClass(File(args[0]).readBytes())
}

fun processSingleClass(byteArray: ByteArray, debug: Boolean = false) {
    if (debug) Bootstrapper.debug = true
    val cl = ClassReader(byteArray)
    var cn = ClassNode()
    cl.accept(cn, ClassReader.SKIP_DEBUG)
    classes.add(cn)
    numberBitwise.transform()
    invokeDynamic.transform()
    switchMangler.transform()
    stupidTransformer.transform()
    fakeGoto.transform()
    // invisibleCast.transform()
    cn = classes.first()

    val cw = ClassWriter(
        if (debug) ClassWriter.COMPUTE_MAXS else ClassWriter.COMPUTE_FRAMES
    )

    cn.accept(cw)
    val out = File("out.class")
    out.createNewFile()
    out.writeBytes(cw.toByteArray())
}

fun process(inFile: File, outFile: File, debug: Boolean = false) {
    val jar = JarFile(inFile)
    jar.use {
        for (i in it.entries()) {
            if (!i.isDirectory && i.name.endsWith(".class") && i.name != "module-info.class") {
                val istream = jar.getInputStream(i)
                val classNode = ClassNode()

                try {
                    ClassReader(istream).accept(classNode, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
                    if (isExcluded(classNode)) {
                        resources[i.name] = jar.getInputStream(i).readBytes()
                    } else {
                        classes.add(classNode)
                    }
                } catch (t: Throwable) {
                    println("Error while reading class ${i.realName}: ${t.javaClass.simpleName}")
                    t.printStackTrace()
                } finally {
                    istream.close()
                    continue
                }
            } else if (!i.isDirectory && (i.name != "module-info.class" || !i.name.endsWith(".class"))) {
                val bytes = jar.getInputStream(i).readBytes()
                resources[i.name] = bytes
            }
        }
    }

    numberBitwise.transform()
    invokeDynamic.transform()
    stringEncryptor.transform()
    switchMangler.transform()
    stupidTransformer.transform()
    fakeGoto.transform()

    JarOutputStream(outFile.outputStream()).use {
        classes.forEach { classNode ->
            val cw: ClassWriter = if (debug) {
                ClassWriter(ClassWriter.COMPUTE_MAXS)
            } else {
                ClassWriter(ClassWriter.COMPUTE_FRAMES)
            }

            val entry = JarEntry("${classNode.name}.class")
            it.putNextEntry(entry)
            classNode.accept(cw)
            it.write(cw.toByteArray())
            it.closeEntry()
        }

        resources.keys.forEach { name ->
            val entry = JarEntry(name)
            it.putNextEntry(entry)
            resources[name]?.let { it1 -> it.write(it1) }
            it.closeEntry()
        }
    }
}

fun isExcluded(classNode: ClassNode): Boolean {
    if (Modifier.isInterface(classNode.access)) return true
    return !classNode.name.contains("neptunex")
}