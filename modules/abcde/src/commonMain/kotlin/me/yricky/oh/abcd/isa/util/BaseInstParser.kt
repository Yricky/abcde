package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.abcd.isa.InstFmt
import me.yricky.oh.utils.value

object BaseInstParser:InstDisAsmParser {
    override fun parseArg(asmItem: Asm.AsmItem, index: Int): String? {
        val arg = asmItem.opRand.value[index]
        val format = asmItem.ins.format
        val m = asmItem.asm.code.method
        return when(val argSig = format[index]){
            is InstFmt.OpCode,is InstFmt.Prefix -> null
            is InstFmt.ImmI -> "$arg"
            is InstFmt.ImmU -> when(arg){
                is Byte -> arg.toUByte().toULong()
                is Short -> arg.toUShort().toULong()
                is Int -> arg.toUInt().toULong()
                is Long -> arg.toULong()
                else -> arg.toLong().toULong()
            }.let { "$it" }
            is InstFmt.ImmF -> when(arg){
                is Int -> "${Float.fromBits(arg)}"
                is Long -> "${Double.fromBits(arg)}"
                else -> throw IllegalStateException("invalid float bitSize:${argSig.bitSize}")
            }
            is InstFmt.RegV -> "v${arg}"
            is InstFmt.MId -> {
                val value = arg.toUnsignedInt().let { m.region.mslIndex[it] }
                val method = m.abc.method(value)
                if(asmItem.asm.code.method.clazz == method.clazz){
                    "this.${method.name}"
                } else "${method.clazz.name}.${method.name}"
            }
            is InstFmt.LId -> {
                val value = arg.toUnsignedInt()
                val literalArray = m.abc.literalArray(m.region.mslIndex[value])
                "$literalArray"
            }
            is InstFmt.SId -> {
                val value = arg.toUnsignedInt()
                val str = m.abc.stringItem(m.region.mslIndex[value])
                "\"${str.value}\""
            }
        }
    }
}