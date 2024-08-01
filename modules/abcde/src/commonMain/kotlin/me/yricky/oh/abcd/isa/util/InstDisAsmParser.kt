package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm

interface InstDisAsmParser {
    /**
     * 将单条指令的参数反汇编为字符串
     * @param index [Asm.AsmItem.opUnits]的下标
     */
    fun parseArg(asmItem: Asm.AsmItem, index:Int):String?
}