package me.yricky.oh.abcd.code

import me.yricky.LEByteBuf
import me.yricky.oh.abcd.cfm.MethodItem
import me.yricky.oh.abcd.isa.*
import me.yricky.oh.utils.nextOffset
import me.yricky.oh.utils.readULeb128
import me.yricky.oh.utils.value

class Code(
    val m:MethodItem,
    val offset:Int
) {
    private val abc get() = m.abc
    private val _numVRegs by lazy { abc.buf.readULeb128(offset) }
    val numVRegs get() = _numVRegs.value
    private val _numArgs by lazy { abc.buf.readULeb128(_numVRegs.nextOffset) }
    val numArgs get() = _numArgs.value
    private val _codeSize by lazy { abc.buf.readULeb128(_numArgs.nextOffset) }
    val codeSize get() = _codeSize.value
    private val _triesSize by lazy { abc.buf.readULeb128(_codeSize.nextOffset) }
    val triesSize get() = _triesSize.value
    val instructions: LEByteBuf by lazy {
        abc.buf.slice(_triesSize.nextOffset,_codeSize.value)
    }
    val tryBlocks:List<TryBlock> by lazy {
        var off = _triesSize.nextOffset+_codeSize.value
        val list = ArrayList<TryBlock>(triesSize)
        repeat(triesSize){
            val tb = TryBlock(abc, off)
            list.add(tb)
            off = tb.nextOff
        }
        list
    }

    val asm by lazy { Asm(this) }
}