package me.yricky.oh.abcd

import me.yricky.oh.common.BufOffset
import me.yricky.oh.common.LEByteBuf

interface AbcBufOffset: BufOffset {
    val abc: AbcBuf
    override val buf: LEByteBuf
        get() = abc.buf
}