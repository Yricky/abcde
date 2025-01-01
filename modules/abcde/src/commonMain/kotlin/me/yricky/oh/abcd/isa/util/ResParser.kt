package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.InstFmt
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.resde.ResIndexBuf

class ResParser(private val res: ResIndexBuf): InstDisAsmParser {
    override fun id(): String = "${InstDisAsmParser.ID_PREFIX}.res"
    override fun title(): String = "资源索引解析扩展"
    override fun description(): String = "将字节码中的资源索引解析为可读格式"

    override fun parseArg(asmItem: Asm.AsmItem, index: Int): String? {
        val format = asmItem.ins.format
        return when(val argSig = format[index]){
            is InstFmt.LId -> {
                val literalArray = argSig.getLA(asmItem)
                parseResLiteral(asmItem.asm,literalArray)
                    ?.let { resIndex -> res.resMap[resIndex] }
                    ?.let {
                        it.find { it.limitKey.contains("zh") } ?: it.firstOrNull()
                    }?.let { "\$r(\"${it.fileName}\")" }
            }
            else -> null
        }
    }

    companion object{
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