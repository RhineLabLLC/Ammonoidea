package rhinelab.ammonoidea

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.transformer.flow.fakeGoto
import rhinelab.ammonoidea.transformer.flow.invokeDynamic
import rhinelab.ammonoidea.transformer.flow.switchMangler
import rhinelab.ammonoidea.transformer.invisibleCast
import rhinelab.ammonoidea.transformer.numberBitwise
import java.io.File

object Bootstrapper {
    var classes = ArrayList<ClassNode>()
    var debug = false
}

fun main(args: Array<String>) {
    if (args.isEmpty()) return
    process(File(args[0]).readBytes())
}

fun process(byteArray: ByteArray, debug: Boolean = false) {
    if (debug) Bootstrapper.debug = true
    val cl = ClassReader(byteArray)
    var cn = ClassNode()
    cl.accept(cn, ClassReader.SKIP_DEBUG)
    classes.add(cn)
    invokeDynamic.transform()
    switchMangler.transform()
    fakeGoto.transform()
    numberBitwise.transform()
    // invisibleCast.transform()
    cn = classes.first()
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cn.accept(cw)
    val out = File("out.class")
    out.createNewFile()
    out.writeBytes(cw.toByteArray())
}