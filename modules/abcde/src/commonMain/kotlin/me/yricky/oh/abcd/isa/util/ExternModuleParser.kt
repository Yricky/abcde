package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.InstFmt

object ExternModuleParser:InstDisAsmParser {
    override fun id(): String = "${InstDisAsmParser.ID_PREFIX}.extern_module"
    override fun title(): String = "ExternModule拓展"
    override fun description(): String = "将字节码中的模块导入指令解析为可读格式"

    override fun parseArg(asmItem: Asm.AsmItem, index: Int): InstDisAsmParser.ParsedArg? {
        val needHandle = asmItem.ins.asmName.endsWith("ldexternalmodulevar") ||
                asmItem.ins.asmName.endsWith("ldsendableexternalmodulevar") ||
                asmItem.ins.asmName.endsWith("ldlazymodulevar")
        if(!needHandle){
            return null
        }
        val args = asmItem.opUnits
        val format = asmItem.ins.format
        val m = asmItem.asm.code.method
        return when(format[index]){
            is InstFmt.ImmU -> when(val arg = args[index]){
                is Byte -> arg.toUByte().toULong()
                is Short -> arg.toUShort().toULong()
                is Int -> arg.toUInt().toULong()
                is Long -> arg.toULong()
                else -> arg.toLong().toULong()
            }.let {
                val clazz = m.clazz
                if(clazz != null){
                    InstDisAsmParser.ParsedArg.plainText("${clazz.moduleInfo?.regularImports?.getOrNull(it.toInt()) ?: it}")
                } else { null }
            }
            else -> null
        }
    }
}