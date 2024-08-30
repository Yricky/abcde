package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.InstFmt

object V2AInstParser:InstDisAsmParser {
    override fun id(): String = "${InstDisAsmParser.ID_PREFIX}.v2a"
    override fun title(): String = "参数寄存器解析插件"
    override fun description(): String = "解析字节码中的参数寄存器"

    override fun parseArg(asmItem: Asm.AsmItem, index: Int): String? {
        val arg = asmItem.opUnits[index]
        val format = asmItem.ins.format
        return when(format[index]){
            is InstFmt.RegV -> {
                val v = arg.toInt()
                val vRegs = asmItem.asm.code.numVRegs
                if (v < vRegs) "v${v}" else when(val aIndex = v - vRegs){
                    0 -> "FunctionObject"
                    1 -> "NewTarget"
                    2 -> "this"
                    else -> "arg${aIndex - 3}"
                }
            }
            else -> null
        }
    }
}