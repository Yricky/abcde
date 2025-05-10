package me.yricky.oh.utils

import me.yricky.oh.common.LEByteBuf
import kotlin.experimental.and

@JvmInline
value class IntAndNextOff(val inner : Long){
    val value get() = (inner shr 32).toInt()
    val nextOffset get() = (inner and 0xffffffffL).toInt()

    constructor(value:Int,nextOff:Int):this((value.toLong() shl 32) or nextOff.toLong())
}

fun LEByteBuf.readULeb128(index:Int): IntAndNextOff {
    var result = 0
    var off = 0
    var byte:Byte
    do {
        byte = get(index + off)
        ++off
        result = result or (byte.and(0x7f).toInt().shl(7*off - 7))
    } while ((byte and 0x80.toByte() != 0.toByte()) && off < 5)
    return IntAndNextOff(result,index + off)
}

fun Int.uleb2sleb():Int{
    var bitSize = 32
    var bitFlag = 0x80000000.toInt()
    while ((this and bitFlag) == 0 && bitFlag != 0){
        bitFlag = bitFlag.ushr(1)
        bitSize--
    }
    return if(bitSize != 0 && bitSize % 7 == 0){
        this or (-1).shl(bitSize)
    } else this
}

fun LEByteBuf.readSLeb128(index:Int): IntAndNextOff {
    var result = 0
    var off = 0
    var byte:Byte
    do {
        byte = get(index + off)
        ++off
        result = result or (byte.and(0x7f).toInt().shl(7*off - 7))
    } while ((byte and 0x80.toByte() != 0.toByte()) && off < 5)

    if((byte and 0x40.toByte()) != 0x00.toByte() && off < 5){
        //符号位为1
        result = result or (-1).shl(off * 7)
    }
    return IntAndNextOff(result,index + off)
}