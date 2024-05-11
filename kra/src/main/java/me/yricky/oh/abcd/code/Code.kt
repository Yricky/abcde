package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.isa.*
import me.yricky.oh.utils.afterOffset
import me.yricky.oh.utils.readULeb128
import me.yricky.oh.utils.value
import java.nio.ByteOrder

class Code(
    val abc:AbcBuf,
    val offset:Int
) {
    private val _numVRegs by lazy { abc.buf.readULeb128(offset) }
    val numVRegs get() = _numVRegs.value
    private val _numArgs by lazy { abc.buf.readULeb128(_numVRegs.afterOffset) }
    val numArgs get() = _numArgs.value
    private val _codeSize by lazy { abc.buf.readULeb128(_numArgs.afterOffset) }
    val codeSize get() = _codeSize.value
    private val _triesSize by lazy { abc.buf.readULeb128(_codeSize.afterOffset) }
    val triesSize get() = _triesSize.value
    val instructions by lazy {
        abc.buf.slice(_triesSize.afterOffset,_codeSize.value).order(ByteOrder.LITTLE_ENDIAN)
    }

    val asm:String by lazy {
        val sb = StringBuilder()
        var off = 0
        while (off < codeSize){
            val initOff = off
            val opCode = instructions.get(off)
            off += 1
            val ins = Ins.INS_MAP[opCode] ?: throw IllegalStateException("No this opCode:${opCode.toString(16)}")
            when(ins){
                is Ins.Ins1 -> {
                    sb.append(ins.symbol).append(' ')
                    ins.fmt.units.forEach {
                        when(it){
                            is IMM8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("%02X,",value))
                            }
                            ID16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                off += 2
                                sb.append(String.format("@%04X,",value))
                            }
                            IMM16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                off += 2
                                sb.append(String.format("%04X,",value))
                            }
                            IMM32 -> {
                                val value = instructions.getInt(off)
                                off += 4
                                sb.append(String.format("%08X,",value))
                            }
                            IMM4IMM4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("[44]%02X,",value))
                            }
                            IMM64 -> {
                                val value = instructions.getLong(off)
                                off += 8
                                sb.append(String.format("%16X,",value))
                            }
                            V16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                off += 2
                                sb.append(String.format("V%04X,",value))
                            }
                            V4V4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("[vv]%02X,",value))
                            }
                            V8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("v%02X,",value))
                            }
                        }
                    }
                }
                is Ins.Ins2 -> {
                    sb.append(ins.symbol).append('.')
                    val subOpCode = instructions.get(off)
                    off += 1
                    val subIns = ins.map[subOpCode] ?: throw IllegalStateException("No this subOpCode:${subOpCode.toString(16)} in opCode:${opCode.toString(16)}")
                    sb.append(subIns.symbol).append(' ')
                    subIns.fmt.units.forEach {
                        when(it){
                            is IMM8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("%02X,",value))
                            }
                            ID16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                off += 2
                                sb.append(String.format("@%04X,",value))
                            }
                            IMM16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                off += 2
                                sb.append(String.format("%04X,",value))
                            }
                            IMM32 -> {
                                val value = instructions.getInt(off)
                                off += 4
                                sb.append(String.format("%08X,",value))
                            }
                            IMM4IMM4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("[44]%02X,",value))
                            }
                            IMM64 -> {
                                val value = instructions.getLong(off)
                                off += 8
                                sb.append(String.format("%16X,",value))
                            }
                            V16 -> {
                                val value = instructions.getShort(off).toUShort().toInt()
                                off += 2
                                sb.append(String.format("V%04X,",value))
                            }
                            V4V4 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("[vv]%02X,",value))
                            }
                            V8 -> {
                                val value = instructions.get(off).toUByte().toInt()
                                off += 1
                                sb.append(String.format("v%02X,",value))
                            }
                        }
                    }

                }
            }
            if(sb.last() == ','){
                sb.deleteCharAt(sb.lastIndex)
            }
            sb.append("\t//")
            (initOff until off).forEach {
                sb.append(String.format("%02X",instructions.get(it)))
            }
            sb.append('\n')
        }
        sb.toString()
    }
}