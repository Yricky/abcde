package me.yricky.oh.utils

import me.yricky.LEByteBuf
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun wrapAsLEByteBuf(buffer: ByteBuffer):LEByteBuf {
    return object :LEByteBuf{
        override fun get(index: Int, dst: ByteArray){
            buffer.get(index, dst)
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