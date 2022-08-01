package rhinelab.ammonoidea.transformer.flow

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import rhinelab.ammonoidea.transformer.transformer

val stupidTransformer = transformer {

}

private class StupidTransformer() : MethodVisitor(Opcodes.ASM9) {
    override fun visitJumpInsn(opcode: Int, label: Label) {
        if (opcode == Opcodes.GOTO) return
    }
}


private fun negate(opcode: Int) {
    when(opcode) {
        IFEQ -> IFNE
        IFNE -> IFEQ
        IF_ACMPEQ -> IF_ACMPNE
        IF_ACMPNE -> IF_ACMPEQ

    }
}