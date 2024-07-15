package me.yricky.oh.common

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun wrapAsLEByteBuf(_buffer: ByteBuffer): LEByteBuf {
    val buffer = if(_buffer.order() != ByteOrder.LITTLE_ENDIAN){
        _buffer.order(ByteOrder.LITTLE_ENDIAN)
    } else _buffer
    return object : LEByteBuf {
        override fun get(index: Int, dst: ByteArray, length:Int){
            buffer.slice(index,length).order(ByteOrder.LITTLE_ENDIAN).get(dst,0,length)
        }

        override fun get(index: Int): Byte = buffer.get(index)

        override fun getShort(index: Int): Short = buffer.getShort(index)

        override fun getInt(index: Int): Int = buffer.getInt(index)

        override fun getLong(index: Int): Long = buffer.getLong(index)

        override fun limit(): Int = buffer.limit()
        override fun slice(index: Int, length: Int): LEByteBuf {
            return wrapAsLEByteBuf(buffer.slice(index, length).order(ByteOrder.LITTLE_ENDIAN))
        }

    }
}