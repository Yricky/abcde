package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.InstFmt
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.resde.ResIndexBuf

class ResParser(private val res: ResIndexBuf): InstDisAsmParser {

    override fun id(): String = ID
    override fun title(): String = "资源索引解析扩展"
    override fun description(): String = "将字节码中的资源索引解析为可读格式"

    override fun parseArg(asmItem: Asm.AsmItem, index: Int): InstDisAsmParser.ParsedArg? {
        val format = asmItem.ins.format
        return when(val argSig = format[index]){
            is InstFmt.LId -> {
                val literalArray = argSig.getLA(asmItem)
                val resIdx = parseResLiteral(asmItem.asm,literalArray)
                resIdx?.let { resIndex -> res.resMap[resIndex] }
                    ?.let {
                        it.find { it.limitKey.contains("zh") } ?: it.firstOrNull()
                    }?.let { ParsedArgRes("\$r(\"${it.fileName}\")", resIdx) }
            }
            else -> null
        }
    }

    companion object{
        const val TAG_RES_INDEX = "RES_IDX"
        const val TAG_VALUE_RES_IDX = "resIdx"
        const val ID = "${InstDisAsmParser.ID_PREFIX}.res"

        class ParsedArgRes(
            override val text: String,
            resIndex:Int
        ): InstDisAsmParser.ParsedArg{
            override val tags: List<String> = listOf(TAG_RES_INDEX)
            override val tagValues: Map<String, InstDisAsmParser.ParsedArg.TagValue> = mapOf(TAG_VALUE_RES_IDX to InstDisAsmParser.ParsedArg.TagValue(
                "$resIndex", 0,text.length
            ))
        }
        fun parseResLiteral(asm: Asm, literalArray: LiteralArray):Int?{
            if(literalArray.content.size % 2 == 0){
                val objKv = mutableMapOf<String,LiteralArray.Literal>()
                val iter = literalArray.content.iterator()
                while (iter.hasNext()){
                    val next = iter.next()
                    if(next is LiteralArray.Literal.Str){
                        objKv[next.get(asm.code.abc)] = iter.next()
                    } else break
                }
                if(
                    objKv["id"] is LiteralArray.Literal.I32 &&
                    objKv["type"] is LiteralArray.Literal.I32 &&
                    objKv["params"] != null &&
                    objKv["bundleName"] is LiteralArray.Literal.Str &&
                    objKv["moduleName"] is LiteralArray.Literal.Str
                ){
                    return (objKv["id"] as LiteralArray.Literal.I32).value
                }
            }
            return null
        }
    }
}