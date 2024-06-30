package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.utils.value

interface InstDisAsmParser {
    fun parseArg(asmItem: Asm.AsmItem, index:Int):String?
    companion object{
        fun asmString(item:Asm.AsmItem, externalParser:List<InstDisAsmParser> = emptyList()):String{
            val sb = StringBuilder()
            item.opRand.value.indices.forEach {
                var argString:String? = null
                val pIterator = externalParser.listIterator()
                while (pIterator.hasNext() && argString == null){
                    argString = pIterator.next().parseArg(item,it)
                }
                if(argString == null){
                    argString = BaseInstParser.parseArg(item,it)
                }
                if(argString != null){
                    sb.append(argString).append(' ')
                }
            }
            return sb.toString()
        }
    }
}