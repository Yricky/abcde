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
    fun parseArg(asmItem: Asm.AsmItem, index:Int): ParsedArg?

    companion object{
        const val ID_PREFIX = "abcde.asm.parser"
        fun ParsedArg(text: String, tags: List<String>,tagValues: Map<String, ParsedArg.TagValue>): ParsedArg = ParsedArgImpl(text,tags,tagValues)
    }

    interface ParsedArg{
        val text: String
        //至少有一个tag
        val tags: List<String>
        val tagValues: Map<String, TagValue>
        companion object{
            private val plainTag = listOf("text")
            fun plainText(text: String): ParsedArg = ParsedArg(text,plainTag,emptyMap())
        }

        class TagValue(
            val data: String,
            val start: Int,
            val endExclusive:Int,
        )
    }

    class ParsedArgImpl(
        override val text: String,
        //至少有一个tag
        override val tags: List<String>,
        override val tagValues: Map<String, ParsedArg.TagValue>
    ): ParsedArg
}