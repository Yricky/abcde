package me.yricky.oh.common

interface LEByteBuf {
    fun get(index:Int, dst:ByteArray)
    fun get(index:Int):Byte
    fun getShort(index:Int):Short
    fun getInt(index:Int):Int
    fun getLong(index:Int):Long

    fun limit():Int
    fun slice(index:Int,length:Int): LEByteBuf
}