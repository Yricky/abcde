package me.yricky.oh.abcd.isa

import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.ForeignMethod
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.isa.bean.InsGroup
import me.yricky.oh.abcd.isa.bean.Instruction
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.utils.value

class Inst(
    val group:InsGroup,
    val instruction: Instruction,
    val opCode:Byte,
    val format:List<InstFmt>
) {
    private val sigSplit by lazy {
        instruction.sig.split(' ').map { it.removeSuffix(",") }
    }

    fun argSize():Int{
        return format.fold(0){ acc,it ->
            if(it is InstFmt.Prefix || it is InstFmt.OpCode){
                acc
            } else acc + it.bitSize
        } / 8
    }

    val asmName get() = sigSplit[0]
    val fmtString:String get() = instruction.format[instruction.opcodeIdx.indexOf(opCode)]

    fun asmString(code: Code,args:List<Number>):String{
        val sb = StringBuilder()
        sb.append(asmName)
        sb.append(' ')
        with(code){
            (1 until sigSplit.size).forEach {
                val argSig = sigSplit[it]
                when{
                    argSig.startsWith("imm") -> sb.append(args[it - 1]).append(' ')
                    argSig.startsWith("v") -> sb.append('v').append(args[it - 1]).append(' ')
                    argSig == "method_id" -> {
                        val value = args[it - 1].toInt().let { m.region.mslIndex[it] }
                        val method = if(m.abc.isForeignOffset(value)) ForeignMethod(m.abc,value) else AbcMethod(m.abc,value)
                        sb.append("${method.clazz.name}.${method.name} ")
                    }
                    argSig == "literalarray_id" -> {
                        val value = args[it - 1].toInt()
                        val literalArray = LiteralArray(m.abc,m.region.mslIndex[value])
                        sb.append("$literalArray ")
                    }
                    argSig == "string_id" -> {
                        val value = args[it - 1].toInt()
                        val str = m.abc.stringItem(m.region.mslIndex[value])
                        sb.append("\"${str.value}\" ")
                    }
                    else -> {
                        sb.append("(${argSig})${args[it-1]} ")
                    }
                }
            }
        }
        return sb.toString()
    }
    companion object{
        fun fromInstructionBean(
            group: InsGroup,
            instruction: Instruction,
            index: Int
        ):Inst{
            return Inst(
                group,
                instruction,
                instruction.opcodeIdx[index],
                InstFmt.fromString(instruction.format[index])
            )
        }
    }

    sealed class InstFmt(val bitSize:Int){
        companion object{
            fun fromString(str:String):List<InstFmt>{
                val strListIter = str.split('_').iterator()
                val res = mutableListOf<InstFmt>()
                while (strListIter.hasNext()){
                    val thisFmt = strListIter.next()
                    val instFmt = when{
                        thisFmt == "pref" -> Prefix()
                        thisFmt == "op" -> OpCode()
                        thisFmt.startsWith("imm") -> {
                            val bitSize = strListIter.next().toInt()
                            val index = if(thisFmt.length == 4){
                                thisFmt.last().code - '0'.code
                            } else null
                            Imm(index, bitSize)
                        }
                        thisFmt.startsWith("id") -> {
                            val bitSize = strListIter.next().toInt()
                            val index = if(thisFmt.length == 3){
                                thisFmt.last().code - '0'.code
                            } else null
                            Id(index, bitSize)
                        }
                        thisFmt.startsWith("v") -> {
                            val bitSize = strListIter.next().toInt()
                            val index = if(thisFmt.length == 2){
                                thisFmt.last().code - '0'.code
                            } else null
                            RegV(index, bitSize)
                        }
                        else -> null
                    }
                    if(instFmt != null){
                        res.add(instFmt)
                    }
                }
                return res
            }
        }
        class Prefix(/*code:Byte*/):InstFmt(8)
        class OpCode(/*code:Byte*/):InstFmt(8)

        /**
         * @param index 0表示只有这一个这个类型的操作数
         */
        class Imm(val index:Int? = 0, bitSize:Int):InstFmt(bitSize)
        class RegV(val index: Int? = 0, bitSize: Int):InstFmt(bitSize)
        class Id(val index: Int? = 0, bitSize: Int):InstFmt(bitSize)
    }
}