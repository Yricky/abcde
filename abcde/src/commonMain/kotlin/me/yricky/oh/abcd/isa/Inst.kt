package me.yricky.oh.abcd.isa

import me.yricky.oh.abcd.isa.bean.InsGroup
import me.yricky.oh.abcd.isa.bean.Instruction

class Inst(
    val group: InsGroup,
    val instruction: Instruction,
    private val index:Int
) {
    val opCode get() = instruction.opcodeIdx[index].toByte()
    val format:List<InstFmt>
    val asmName:String

    init {
        val sigSplit = instruction.sig.split(' ').map { it.removeSuffix(",") }
        asmName = sigSplit[0]
        format = InstFmt.fromString(instruction.format[index],sigSplit)
    }

    /**
     * 这条指令去掉prefix和opCode剩余的部分的size，单位为Byte
     */
    fun argSize():Int{
        return format.fold(0){ acc,it ->
            if(it is InstFmt.Prefix || it is InstFmt.OpCode){
                acc
            } else acc + it.bitSize
        } / 8
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

}