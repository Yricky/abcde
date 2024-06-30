package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.FieldType
import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.InstFmt
import me.yricky.oh.utils.value

object ExternModuleParser:InstDisAsmParser {
    override fun parseArg(asmItem: Asm.AsmItem, index: Int): String? {
        val args = asmItem.opRand.value
        val format = asmItem.ins.format
        val m = asmItem.asm.code.m
        if(asmItem.ins.opCode != 0x7e.toByte() && asmItem.ins.opCode != 0x11.toByte()){
            return null
        }
        return when(format[index]){
            is InstFmt.ImmU -> when(val arg = args[index]){
                is Byte -> arg.toUByte().toULong()
                is Short -> arg.toUShort().toULong()
                is Int -> arg.toUInt().toULong()
                is Long -> arg.toULong()
                else -> arg.toLong().toULong()
            }.let {
                val clazz = (m.clazz as? FieldType.ClassType)?.clazz as? AbcClass
                if(clazz != null){
                    "${clazz.moduleInfo?.regularImports?.getOrNull(it.toInt()) ?: it}"
                } else { null }
            }
            else -> null
        }
    }
}