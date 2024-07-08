package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcBufOffset

class LineNumberParam(override val abc: AbcBuf, override val offset: Int) :AbcBufOffset {


    sealed class Item(val opCode:Byte)
    data object End:Item(0)
    class AdvancePc:Item(0x01)
    class AdvancePLine:Item(0x02)
    class StartLocal:Item(0x03)
}