package me.yricky.oh.abcd.isa

sealed class InstFmt(val bitSize:Int){
    companion object{
        fun fromString(fmt:String,sigSplit:List<String>):List<InstFmt>{
            val strListIter = fmt.split('_').iterator()
            val sigIter = sigSplit.iterator().apply { /* 跳过name段 */ next() }
            val res = mutableListOf<InstFmt>()
            while (strListIter.hasNext()){
                val thisFmt = strListIter.next()
                val instFmt = when{
                    thisFmt == "pref" -> Prefix()
                    thisFmt == "op" -> OpCode()
                    thisFmt == "none" -> null
                    thisFmt.startsWith("imm") -> {
                        val thisSig = sigIter.next()
                        val bitSize = strListIter.next().toInt()
                        val index = if(thisFmt.length == 4){
                            thisFmt.last().code - '0'.code
                        } else null
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
                        val index = if(thisFmt.length == 3){
                            thisFmt.last().code - '0'.code
                        } else null
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
                        val index = if(thisFmt.length == 2){
                            thisFmt.last().code - '0'.code
                        } else null
                        RegV(index, bitSize)
                    }
                    else -> throw IllegalStateException("no such fmt:${thisFmt}")
                }
                if(instFmt != null){
                    res.add(instFmt)
                }
            }
            return res
        }
    }
    class Prefix(/*code:Byte*/): InstFmt(8)
    class OpCode(/*code:Byte*/): InstFmt(8)

    /**
     * @param index 0表示只有这一个这个类型的操作数
     */
    class ImmI(val index:Int? = 0, bitSize:Int): InstFmt(bitSize)
    class ImmU(val index:Int? = 0, bitSize:Int): InstFmt(bitSize)
    class ImmF(val index:Int? = 0, bitSize:Int): InstFmt(bitSize)
    class RegV(val index: Int? = 0, bitSize: Int): InstFmt(bitSize)
    class SId(val index: Int? = 0, bitSize: Int): InstFmt(bitSize)
    class MId(val index: Int? = 0, bitSize: Int): InstFmt(bitSize)
    class LId(val index: Int? = 0, bitSize: Int): InstFmt(bitSize)
}