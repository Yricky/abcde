package me.yricky.oh.abcd

class LiteralArray(
    val abc:AbcBuf,
    val offset:Int
) {
    val _size by lazy {
        abc.buf.getInt(offset)
    }
    val size get() = if(_size % 2 == 0){ _size.ushr(1) } else _size.ushr(2)
    val flag get() = _size % 2 != 0
}