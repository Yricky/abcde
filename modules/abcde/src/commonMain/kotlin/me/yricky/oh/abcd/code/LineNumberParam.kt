package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcBufOffset

class LineNumberParam(override val abc: AbcBuf, override val offset: Int) :AbcBufOffset {


    sealed class Item(val opCode:Byte)
    data object End:Item(0)
}