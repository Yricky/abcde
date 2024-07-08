package me.yricky.oh.resde

import me.yricky.oh.common.LEByteBuf

class ResIndexHeader(
    buffer:LEByteBuf
){
    val version:ByteArray = ByteArray(128).also {
        buffer.get(0,it)
    } //size = 128
    val fileSize:Int = buffer.getInt(128)
    val limitKeyConfigCount:Int = buffer.getInt(128 + 4)

    companion object{
        const val SIZE = 136
    }
}