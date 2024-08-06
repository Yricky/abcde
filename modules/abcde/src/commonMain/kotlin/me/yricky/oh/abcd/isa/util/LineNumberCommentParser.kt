package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.code.address
import me.yricky.oh.abcd.code.column
import me.yricky.oh.abcd.code.line
import me.yricky.oh.abcd.isa.Asm

object LineNumberCommentParser:InstCommentParser {
    override fun parse(asmItem: Asm.AsmItem): String? {
        return asmItem.asm.code.method.debugInfo?.state?.addressLineColumns?.firstOrNull {
            it.address == asmItem.codeOffset && it.line >= 0 && it.column >= 0
        }?.let { "${it.line}:${it.column}" }

    }
}