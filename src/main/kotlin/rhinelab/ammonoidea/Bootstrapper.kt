package rhinelab.ammonoidea

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import rhinelab.ammonoidea.Bootstrapper.classes
import rhinelab.ammonoidea.transformer.flow.fakeGoto
import rhinelab.ammonoidea.transformer.flow.invokeDynamic
import rhinelab.ammonoidea.transformer.flow.switchMangler
import java.io.File

object Bootstrapper {
    var classes = ArrayList<ClassNode>()
    var debug = false
}

fun main(args: Array<String>) {
    if (args.isEmpty()) return
    process(File(args[0]).readBytes())
}