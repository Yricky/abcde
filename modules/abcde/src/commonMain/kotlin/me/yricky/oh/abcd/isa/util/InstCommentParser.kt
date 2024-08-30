package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm

interface InstCommentParser {
    fun title():String? = null
    fun description():String? = null

    fun parse(asmItem: Asm.AsmItem):String?
    companion object{
        const val ID_PREFIX = "abcde.asm.comment.parser"

        fun commentString(item: Asm.AsmItem, externalParser:List<InstCommentParser> = listOf(RawByteCommentParser,LineNumberCommentParser)):String{
            val sb = StringBuilder()
            externalParser.forEach {
                val comment = it.parse(item)
                if(comment?.isNotBlank() == true){
                    if(sb.isEmpty()){
                        sb.append("//")
                    } else {
                        sb.append(", ")
                    }
                    sb.append(comment)
                }
            }
            return sb.toString()
        }
    }
}