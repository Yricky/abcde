package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.common.nextOffset

object RawByteCommentParser:InstCommentParser {
    private const val HEX_CHARS = "0123456789ABCDEF"

    override fun parse(asmItem: Asm.AsmItem): String {
        val sb = StringBuilder()
        (asmItem.codeOffset until asmItem.nextOffset).forEach {
            val b = asmItem.asm.code.instructions.get(it).toUByte().toInt()
            sb.append(HEX_CHARS[b/16])
            sb.append(HEX_CHARS[b%16])
        }
        return sb.toString()
    }
}