package me.yricky.oh.abcd.isa

import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.ForeignMethod
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.utils.value

class Asm(
    val code: Code,
) {
    val list:List<AsmItem> by lazy{
        val li = ArrayList<AsmItem>()
        var off = 0
        while (off < code.codeSize){
            val initOff = off
            val opCode = code.instructions.get(off)
            off += 1
            val ins = Ins.INS_MAP[opCode] ?: throw IllegalStateException("No this opCode:${opCode.toString(16)},off:${off - 1}")
            when(ins){
                is Ins.Ins1 -> {
                    off += ins.argSize()
                    li.add(AsmItem(ins, initOff))
                }
                is Ins.Ins2 -> {
                    val subOpCode = code.instructions.get(off)
                    off += 1
                    off += ins.map[subOpCode]?.argSize() ?: throw IllegalStateException("No this subOpCode:${subOpCode.toString(16)} in opCode:${opCode.toString(16)}")
                    li.add(AsmItem(ins, initOff))
                }
            }
        }
        li
    }

    fun asmString(item: AsmItem):String{
        val sb = StringBuilder()
        val initOff = item.codeOffset
        var off = item.codeOffset + 1 //opCode之后的
        with(code){
            when(val ins = item.ins){
                is Ins.Ins1 -> {
                    sb.append(ins.symbol).append(' ')
                    ins.fmt.units.forEach {
                        when(it){
                            is IMM8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("%02X,",value))
                            }
                            LID16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                val literalArray = LiteralArray(m.abc,m.region.mslIndex[value])
                                sb.append("${literalArray},")
                            }
                            SID16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                val str = m.abc.stringItem(m.region.mslIndex[value])
                                sb.append("\"${str.value}\",")
                            }
                            MID16 -> {
                                val value = instructions.getShort(off).toUShort().toInt().let { m.region.mslIndex[it] }
                                val method = if(m.abc.isForeignOffset(value)) ForeignMethod(m.abc,value) else AbcMethod(m.abc,value)
                                sb.append("${method.clazz.name}.${method.name},")
                            }
                            IMM16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                sb.append(String.format("%04X,",value))
                            }
                            IMM32 -> {
                                val value = instructions.getInt(off)
                                sb.append(String.format("%08X,",value))
                            }
                            IMM4IMM4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("[44]%02X,",value))
                            }
                            IMM64 -> {
                                val value = instructions.getLong(off)
                                sb.append(String.format("%16X,",value))
                            }
                            V16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                sb.append(String.format("V%04X,",value))
                            }
                            V4V4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("[vv]%02X,",value))
                            }
                            V8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("v%02X,",value))
                            }
                        }
                        off += it.size
                    }
                }
                is Ins.Ins2 -> {
                    sb.append(ins.symbol).append('.')
                    val subOpCode = instructions.get(off)
                    off += 1
                    val subIns = ins.map[subOpCode] ?: throw IllegalStateException("No this subOpCode:${subOpCode.toString(16)} in opCode:${ins.opCode.toString(16)}")
                    sb.append(subIns.symbol).append(' ')
                    subIns.fmt.units.forEach {
                        when(it){
                            is IMM8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("%02X,",value))
                            }
                            LID16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                val literalArray = LiteralArray(m.abc,m.region.mslIndex[value])
                                sb.append("${literalArray},")
                            }
                            SID16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                val str = m.abc.stringItem(m.region.mslIndex[value])
                                sb.append("\"${str.value}\",")
                            }
                            MID16 -> {
                                println("ins:${ins.symbol}")
                                val value = instructions.getShort(off).toUShort().toInt().let { m.region.mslIndex[it] }
                                val method = if(m.abc.isForeignOffset(value)) ForeignMethod(m.abc,value) else AbcMethod(m.abc,value)
                                sb.append("${method.clazz.name}.${method.name},")
                            }
                            IMM16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                sb.append(String.format("%04X,",value))
                            }
                            IMM32 -> {
                                val value = instructions.getInt(off)
                                sb.append(String.format("%08X,",value))
                            }
                            IMM4IMM4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("[44]%02X,",value))
                            }
                            IMM64 -> {
                                val value = instructions.getLong(off)
                                sb.append(String.format("%16X,",value))
                            }
                            V16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                sb.append(String.format("V%04X,",value))
                            }
                            V4V4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("[vv]%02X,",value))
                            }
                            V8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                sb.append(String.format("v%02X,",value))
                            }
                        }
                        off += it.size
                    }

                }
            }

        }
        if(sb.last() == ','){
            sb.deleteCharAt(sb.lastIndex)
        }
        sb.append(" ".repeat((8 - sb.length%8)))
        sb.append("//")
        (initOff until off).forEach {
            sb.append(String.format("%02X",code.instructions.get(it)))
        }
        return sb.toString()
    }

    class AsmItem(
        val ins:Ins,
        val codeOffset:Int
    )
}