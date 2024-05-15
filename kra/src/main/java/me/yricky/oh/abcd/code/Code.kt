package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.ForeignMethod
import me.yricky.oh.abcd.cfm.MethodItem
import me.yricky.oh.abcd.isa.*
import me.yricky.oh.utils.nextOffset
import me.yricky.oh.utils.readULeb128
import me.yricky.oh.utils.value
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Code(
    val m:MethodItem,
    val offset:Int
) {
    private val abc get() = m.abc
    private val _numVRegs by lazy { abc.buf.readULeb128(offset) }
    val numVRegs get() = _numVRegs.value
    private val _numArgs by lazy { abc.buf.readULeb128(_numVRegs.nextOffset) }
    val numArgs get() = _numArgs.value
    private val _codeSize by lazy { abc.buf.readULeb128(_numArgs.nextOffset) }
    val codeSize get() = _codeSize.value
    private val _triesSize by lazy { abc.buf.readULeb128(_codeSize.nextOffset) }
    val triesSize get() = _triesSize.value
    val instructions: ByteBuffer by lazy {
        abc.buf.slice(_triesSize.nextOffset,_codeSize.value).order(ByteOrder.LITTLE_ENDIAN)
    }
    val tryBlocks:List<TryBlock> by lazy {
        var off = _triesSize.nextOffset+_codeSize.value
        val list = ArrayList<TryBlock>(triesSize)
        repeat(triesSize){
            val tb = TryBlock(abc, off)
            list.add(tb)
            off = tb.nextOff
        }
        list
    }

    val asm by lazy { Asm(this) }
//    :List<String> by lazy {
//        val li = mutableListOf<String>()
//        var off = 0
//        while (off < codeSize){
//            val sb = StringBuilder()
//            val initOff = off
//            val opCode = instructions.get(off)
//            off += 1
//            val ins = Ins.INS_MAP[opCode] ?: throw IllegalStateException("No this opCode:${opCode.toString(16)},off:${off - 1}")
//            when(ins){
//                is Ins.Ins1 -> {
//                    sb.append(ins.symbol).append(' ')
//                    ins.fmt.units.forEach {
//                        when(it){
//                            is IMM8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("%02X,",value))
//                            }
//                            LID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("L@%04X,",value))
//                            }
//                            SID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                val str = abc.stringItem(m.region.mslIndex[value])
//                                sb.append("${str.value},")
//                            }
//                            MID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt().let { m.region.mslIndex[it] }
//                                val method = if(abc.isForeignOffset(value)) ForeignMethod(m.abc,value) else AbcMethod(m.abc,value)
//                                sb.append("${method.clazz.name}.${method.name},")
//                            }
//                            IMM16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("%04X,",value))
//                            }
//                            IMM32 -> {
//                                val value = instructions.getInt(off)
//                                sb.append(String.format("%08X,",value))
//                            }
//                            IMM4IMM4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[44]%02X,",value))
//                            }
//                            IMM64 -> {
//                                val value = instructions.getLong(off)
//                                sb.append(String.format("%16X,",value))
//                            }
//                            V16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("V%04X,",value))
//                            }
//                            V4V4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[vv]%02X,",value))
//                            }
//                            V8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("v%02X,",value))
//                            }
//                        }
//                        off += it.size
//                    }
//                }
//                is Ins.Ins2 -> {
//                    sb.append(ins.symbol).append('.')
//                    val subOpCode = instructions.get(off)
//                    off += 1
//                    val subIns = ins.map[subOpCode] ?: throw IllegalStateException("No this subOpCode:${subOpCode.toString(16)} in opCode:${opCode.toString(16)}")
//                    sb.append(subIns.symbol).append(' ')
//                    subIns.fmt.units.forEach {
//                        when(it){
//                            is IMM8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("%02X,",value))
//                            }
//                            LID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("L@%04X,",value))
//                            }
//                            SID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                val str = abc.stringItem(m.region.mslIndex[value])
//                                sb.append("${str.value},")
//                            }
//                            MID16 -> {
//                                println("ins:${ins.symbol}")
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                val method = if(abc.isForeignOffset(value)) ForeignMethod(m.abc,value) else AbcMethod(m.abc,value)
//                                sb.append("${method.clazz.name}.${method.name},")
//                            }
//                            IMM16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("%04X,",value))
//                            }
//                            IMM32 -> {
//                                val value = instructions.getInt(off)
//                                sb.append(String.format("%08X,",value))
//                            }
//                            IMM4IMM4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[44]%02X,",value))
//                            }
//                            IMM64 -> {
//                                val value = instructions.getLong(off)
//                                sb.append(String.format("%16X,",value))
//                            }
//                            V16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("V%04X,",value))
//                            }
//                            V4V4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[vv]%02X,",value))
//                            }
//                            V8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("v%02X,",value))
//                            }
//                        }
//                        off += it.size
//                    }
//
//                }
//            }
//            if(sb.last() == ','){
//                sb.deleteCharAt(sb.lastIndex)
//            }
//            sb.append(" ".repeat((8 - sb.length%8)))
//            sb.append("//")
//            (initOff until off).forEach {
//                sb.append(String.format("%02X",instructions.get(it)))
//            }
//            li.add(sb.toString())
//        }
//        li.add("\n")
//        li
//    }
}