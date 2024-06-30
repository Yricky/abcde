package me.yricky

import me.yricky.oh.abcd.AbcBuf

interface AbcBufOffset:BufOffset {
    val abc: AbcBuf
    override val buf: LEByteBuf
        get() = abc.buf
}