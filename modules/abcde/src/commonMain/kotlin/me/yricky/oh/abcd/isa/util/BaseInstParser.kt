package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.InstFmt

object BaseInstParser:InstDisAsmParser {
    override fun id(): String = "${InstDisAsmParser.ID_PREFIX}.base"
    override fun title(): String = "基础反汇编器"
    override fun description(): String = "提供基础的反汇编功能"

    override fun parseArg(asmItem: Asm.AsmItem, index: Int): InstDisAsmParser.ParsedArg? {
        val arg = asmItem.opUnits[index]
        val format = asmItem.ins.format
        return when(val argSig = format[index]){
            is InstFmt.OpCode,is InstFmt.Prefix -> null
            is InstFmt.ImmI -> InstDisAsmParser.ParsedArg("$arg",instTags,emptyMap())
            is InstFmt.ImmU -> when(arg){
                is Byte -> arg.toUByte().toULong()
                is Short -> arg.toUShort().toULong()
                is Int -> arg.toUInt().toULong()
                is Long -> arg.toULong()
                else -> arg.toLong().toULong()
            }.let { InstDisAsmParser.ParsedArg.plainText("$it") }
            is InstFmt.ImmF -> when(arg){
                is Int -> "${Float.fromBits(arg)}"
                is Long -> "${Double.fromBits(arg)}"
                else -> throw IllegalStateException("invalid float bitSize:${argSig.bitSize}")
            }.let { InstDisAsmParser.ParsedArg.plainText(it) }
            is InstFmt.RegV -> InstDisAsmParser.ParsedArg.plainText("v${arg}")
            is InstFmt.MId -> {
                val method = argSig.getMethod(asmItem)
                val str = if(asmItem.asm.code.method.clazz == method.clazz){
                    "this.${method.name}"
                } else "${method.clazz?.name}.${method.name}"
                InstDisAsmParser.ParsedArg(str,listOf(TAG_METHOD),mapOf(
                    TAG_VALUE_METHOD_IDX to InstDisAsmParser.ParsedArg.TagValue(
                        "${method.offset}",0,str.length
                    )
                ))
            }
            is InstFmt.LId -> {
                val literalArray = argSig.getLA(asmItem)
                InstDisAsmParser.ParsedArg.plainText("$literalArray")
            }
            is InstFmt.SId -> {
                val str = argSig.getString(asmItem)
                InstDisAsmParser.ParsedArg.plainText("\"${str}\"")
            }
        }
    }

    const val ANNO_ASM_NAME = "ANNO_ASM_NAME"
    private val instTags = listOf(ANNO_ASM_NAME)

    const val TAG_METHOD = "METHOD"
    const val TAG_VALUE_METHOD_IDX = "methodIdx"
}