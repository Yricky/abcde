package me.yricky.oh.common

interface LEByteBuf {
    fun get(index:Int, dst:ByteArray, length: Int = dst.size)
    fun get(index:Int):Byte
    fun getShort(index:Int):Short
    fun getInt(index:Int):Int
    fun getLong(index:Int):Long

    fun limit():Int
    fun slice(index:Int,length:Int): LEByteBuf
}

fun LEByteBuf.toByteArray() = ByteArray(limit()).also { get(0,it) }