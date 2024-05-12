package me.yricky.oh.utils

import java.nio.ByteBuffer
import kotlin.experimental.and

fun ByteBuffer.readULeb128(index:Int): DataAndNextOff<Int> {
    var result = 0
    var off = 0
    var byte:Byte
    do {
        byte = get(index + off)
        ++off
        result = result or (byte.and(0x7f).toInt().shl(7*off - 7))
    } while ((byte and 0x80.toByte() != 0.toByte()) && off < 5)
    return DataAndNextOff(result,index + off)
}

fun ByteBuffer.readSLeb128(index:Int): DataAndNextOff<Int> {
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
    return DataAndNextOff(result,index + off)
}

typealias DataAndNextOff<T> = Pair<T,Int>
val <T> DataAndNextOff<T>.value:T get() = first
val <T> DataAndNextOff<T>.nextOffset:Int get() = second

fun stringItem(buf: ByteBuffer,offset:Int):DataAndNextOff<String>{
    val (utf16Size,strDataOff) = buf.readULeb128(offset)
    return MUtf8.getMUtf8String(buf,strDataOff,utf16Size.ushr(1))
}