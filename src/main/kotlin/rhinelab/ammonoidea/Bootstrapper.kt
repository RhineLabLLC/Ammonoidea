package rhinelab.ammonoidea

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import rhinelab.ammonoidea.transformer.flow.fakeGoto
import rhinelab.ammonoidea.transformer.flow.switchMangler
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) return
    val cl: ClassReader = ClassReader(File(args[0]).inputStream())
    var cn: ClassNode = ClassNode()
    cl.accept(cn, ClassReader.SKIP_DEBUG)
    fakeGoto.classes.add(cn)
    fakeGoto.transform()
    cn = fakeGoto.classes.first()
    switchMangler.classes.add(cn)
    switchMangler.transform()
    cn = switchMangler.classes.first()
    val cw: ClassWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cn.accept(cw)
    val out = File("out.class")
    out.createNewFile()
    out.writeBytes(cw.toByteArray())
}