package me.yricky.oh.utils

import me.yricky.oh.common.LEByteBuf

class Adler32(
    var a:Int = 1,
    var b:Int = 0
) {
    fun update(buf: LEByteBuf){
        var offset = 0
        val size = buf.limit()
        while (offset < size){
            val thisSeg = (size - offset).coerceAtMost(16)
            repeat(thisSeg){
                a += buf.get(offset + it).toUByte().toInt()
                b += a
            }
            a %= 65521
            b %= 65521
            offset += thisSeg
        }
    }

    fun value():Int{
        return b.shl(16).or(a)
    }
}