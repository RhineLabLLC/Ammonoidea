package rhinelab.ammonoidea.transformer.flow

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import rhinelab.ammonoidea.Bootstrapper
import rhinelab.ammonoidea.transformer.transformer

val stupidTransformer = transformer {
    Bootstrapper.classes.forEach {
        it.methods.forEach { m -> m.accept(StupidTransformer()) }
    }
}

private class StupidTransformer : MethodVisitor(ASM9) {
    override fun visitJumpInsn(opcode: Int, label: Label) {
        if (opcode == GOTO) return
        val new = Label()

        super.visitJumpInsn(negate(opcode), new)
        visitLabel(new)
        super.visitJumpInsn(GOTO, label)
    }
}


private fun negate(opcode: Int): Int {
    return when (opcode) {
        IF_ACMPEQ -> IF_ACMPNE
        IF_ACMPNE -> IF_ACMPEQ
        IF_ICMPEQ -> IF_ICMPNE
        IF_ICMPNE -> IF_ICMPEQ
        IF_ICMPLT -> IF_ICMPGE
        IF_ICMPGE -> IF_ICMPLT
        IF_ICMPGT -> IF_ICMPLE
        IF_ICMPLE -> IF_ICMPGT
        IFEQ -> IFNE
        IFNE -> IFEQ
        IFLT -> IFGE
        IFGE -> IFLT
        IFGT -> IFLE
        IFLE -> IFGT
        IFNULL -> IFNONNULL
        IFNONNULL -> IFNULL
        else -> throw IllegalArgumentException("Unknown opcode: $opcode")
    }
}