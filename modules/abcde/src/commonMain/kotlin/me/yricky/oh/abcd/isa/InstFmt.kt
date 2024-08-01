package me.yricky.oh.abcd.isa

import me.yricky.oh.abcd.cfm.MethodItem
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.common.value

sealed class InstFmt(val bitSize:Int){
    companion object{
        fun fromString(fmt:String,sigSplit:List<String>):List<InstFmt>{
            val strListIter = fmt.split('_').iterator()
            val sigIter = sigSplit.iterator().apply { /* 跳过name段 */ next() }
            val res = mutableListOf<InstFmt>()
            var index = 0
            while (strListIter.hasNext()){
                val thisFmt = strListIter.next()
                val instFmt = when{
                    thisFmt == "pref" -> Prefix()
                    thisFmt == "op" -> OpCode()
                    thisFmt == "none" -> null
                    thisFmt.startsWith("imm") -> {
                        val thisSig = sigIter.next()
                        val bitSize = strListIter.next().toInt()
                        when{
                            thisSig.contains(":i") -> ImmI(index, bitSize)
                            thisSig.contains(":u") -> ImmU(index, bitSize)
                            thisSig.contains(":f") -> ImmF(index, bitSize)
                            else -> throw IllegalStateException("invalid imm sig:$thisSig")
                        }

                    }
                    thisFmt.startsWith("id") -> {
                        val thisSig = sigIter.next()
                        val bitSize = strListIter.next().toInt()
                        when(thisSig){
                            "method_id" -> MId(index, bitSize)
                            "literalarray_id" -> LId(index, bitSize)
                            "string_id" -> SId(index, bitSize)
                            else -> throw IllegalStateException("no such id type:$thisSig")
                        }
                    }
                    thisFmt.startsWith("v") -> {
                        val thisSig = sigIter.next()
                        val bitSize = strListIter.next().toInt()
                        RegV(index, bitSize)
                    }
                    else -> throw IllegalStateException("no such fmt:${thisFmt}")
                }
                if(instFmt != null){
                    res.add(instFmt)
                    index++
                }
            }
            return res
        }
    }
    class Prefix(/*code:Byte*/): InstFmt(8)
    class OpCode(/*code:Byte*/): InstFmt(8)

    /**
     * @param index [Asm.AsmItem.opUnits]中的index
     */
    class ImmI(val index:Int, bitSize:Int): InstFmt(bitSize)
    class ImmU(val index:Int, bitSize:Int): InstFmt(bitSize)
    class ImmF(val index:Int, bitSize:Int): InstFmt(bitSize)
    class RegV(val index:Int, bitSize:Int): InstFmt(bitSize)
    class SId( val index:Int, bitSize:Int): InstFmt(bitSize){
        fun getString(item:Asm.AsmItem):String {
            val value = item.opUnits[index].toUnsignedInt()
            return item.asm.code.abc.stringItem(item.asm.code.method.region.mslIndex[value]).value
        }
    }
    class MId( val index:Int, bitSize:Int): InstFmt(bitSize) {
        fun getMethod(item:Asm.AsmItem):MethodItem{
            val value = item.opUnits[index].toUnsignedInt().let { item.asm.code.method.region.mslIndex[it] }
            return item.asm.code.abc.method(value)
        }
    }
    class LId( val index:Int, bitSize:Int): InstFmt(bitSize){
        fun getLA(item:Asm.AsmItem):LiteralArray {
            val value = item.opUnits[index].toUnsignedInt()
            return item.asm.code.abc.literalArray(item.asm.code.method.region.mslIndex[value])
        }
    }
}