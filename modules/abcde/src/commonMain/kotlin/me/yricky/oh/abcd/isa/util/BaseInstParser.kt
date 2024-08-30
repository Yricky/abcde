package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.InstFmt

object BaseInstParser:InstDisAsmParser {
    override fun id(): String = "${InstDisAsmParser.ID_PREFIX}.base"
    override fun title(): String = "基础反汇编器"
    override fun description(): String = "提供基础的反汇编功能"

    override fun parseArg(asmItem: Asm.AsmItem, index: Int): String? {
        val arg = asmItem.opUnits[index]
        val format = asmItem.ins.format
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
                val method = argSig.getMethod(asmItem)
                if(asmItem.asm.code.method.clazz == method.clazz){
                    "this.${method.name}"
                } else "${method.clazz.name}.${method.name}"
            }
            is InstFmt.LId -> {
                val literalArray = argSig.getLA(asmItem)
                "$literalArray"
            }
            is InstFmt.SId -> {
                val str = argSig.getString(asmItem)
                "\"${str}\""
            }
        }
    }
}