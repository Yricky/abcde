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
    private val index:Int
) {
    private val sigSplit by lazy {
        instruction.sig.split(' ').map { it.removeSuffix(",") }
    }

    val opCode get() = instruction.opcodeIdx[index]
    val format:List<InstFmt> by lazy {
        InstFmt.fromString(instruction.format[index],sigSplit)
    }

    fun argSize():Int{
        return format.fold(0){ acc,it ->
            if(it is InstFmt.Prefix || it is InstFmt.OpCode){
                acc
            } else acc + it.bitSize
        } / 8
    }

    val asmName get() = sigSplit[0]

    fun asmString(code: Code,args:List<Number>):String{
        val sb = StringBuilder()
        sb.append(asmName)
        sb.append(' ')
        with(code){
            args.indices.forEach {
                when(val argSig = format[it]){
                    is InstFmt.OpCode,is InstFmt.Prefix -> {}
                    is InstFmt.ImmI -> sb.append(args[it]).append(' ')
                    is InstFmt.ImmU -> when(val arg = args[it]){
                        is Byte -> sb.append(arg.toUByte()).append(' ')
                        is Short -> sb.append(arg.toUShort()).append(' ')
                        is Int -> sb.append(arg.toUInt()).append(' ')
                        is Long -> sb.append(arg.toULong()).append(' ')
                    }
                    is InstFmt.ImmF -> when(val arg = args[it]){
                        is Int -> sb.append(Float.fromBits(arg)).append(' ')
                        is Long -> sb.append(Double.fromBits(arg)).append(' ')
                        else -> throw IllegalStateException("invalid float bitSize:${argSig.bitSize}")
                    }
                    is InstFmt.RegV -> sb.append('v').append(args[it]).append(' ')
                    is InstFmt.MId -> {
                        val value = args[it].toUnsignedInt().let { m.region.mslIndex[it] }
                        val method = m.abc.method(value)
                        sb.append("${method.clazz.name}.${method.name} ")
                    }
                    is InstFmt.LId -> {
                        val value = args[it].toUnsignedInt()
                        val literalArray = LiteralArray(m.abc,m.region.mslIndex[value])
                        sb.append("$literalArray ")
                    }
                    is InstFmt.SId -> {
                        val value = args[it].toUnsignedInt()
                        val str = m.abc.stringItem(m.region.mslIndex[value])
                        sb.append("\"${str.value}\" ")
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
                index
            )
        }

        fun Number.toUnsignedInt():Int = when(this){
            is Byte -> toUByte().toInt()
            is Short -> toUShort().toInt()
            is Int -> {
                if(this < 0){
                    throw IllegalStateException("$this < 0")
                } else this
            }
            is Long -> if(this < 0 || this > 0x7fff_ffffL){
                throw IllegalStateException("$this < 0")
            } else this.toInt()
            else -> throw IllegalStateException("unsupported")
        }
    }

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
        class Prefix(/*code:Byte*/):InstFmt(8)
        class OpCode(/*code:Byte*/):InstFmt(8)

        /**
         * @param index 0表示只有这一个这个类型的操作数
         */
        class ImmI(val index:Int? = 0, bitSize:Int):InstFmt(bitSize)
        class ImmU(val index:Int? = 0, bitSize:Int):InstFmt(bitSize)
        class ImmF(val index:Int? = 0, bitSize:Int):InstFmt(bitSize)
        class RegV(val index: Int? = 0, bitSize: Int):InstFmt(bitSize)
        class SId(val index: Int? = 0, bitSize: Int):InstFmt(bitSize)
        class MId(val index: Int? = 0, bitSize: Int):InstFmt(bitSize)
        class LId(val index: Int? = 0, bitSize: Int):InstFmt(bitSize)
    }
}