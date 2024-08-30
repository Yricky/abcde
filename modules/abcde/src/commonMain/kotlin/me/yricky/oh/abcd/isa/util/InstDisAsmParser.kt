package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm

interface InstDisAsmParser {
    fun id():String
    fun title():String? = null
    fun description():String? = null

    /**
     * 将单条指令的参数反汇编为字符串
     * @param index [Asm.AsmItem.opUnits]的下标
     */
    fun parseArg(asmItem: Asm.AsmItem, index:Int):String?

    companion object{
        const val ID_PREFIX = "abcde.asm.parser"
    }
}