package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.utils.nextOffset

object RawByteCommentParser:InstCommentParser {
    override fun parse(asmItem: Asm.AsmItem): String {
        val sb = StringBuilder()
        (asmItem.codeOffset until asmItem.opRand.nextOffset).forEach {
            val b = asmItem.asm.code.instructions.get(it).toUByte().toInt()
            sb.append("${Asm.HEX_CHARS[b/16]}${Asm.HEX_CHARS[b%16]}")
        }
        return sb.toString()
    }
}