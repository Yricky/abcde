package me.yricky.oh.abcd.code

import me.yricky.oh.BaseOccupyRange
import me.yricky.oh.OffsetRange
import me.yricky.oh.SizeInBuf
import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.common.LEByteBuf
import me.yricky.oh.abcd.isa.*
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value
import me.yricky.oh.utils.IntAndNextOff
import me.yricky.oh.utils.readULeb128

class Code(
    val method:AbcMethod,
    override val offset:Int
): AbcBufOffset, BaseOccupyRange,SizeInBuf.External {
    override val abc get() = method.abc
    val numVRegs:Int
    val numArgs:Int
    val codeSize:Int
    private val _triesSize : IntAndNextOff
    val triesSize:Int get() = _triesSize.value
    val instructions: LEByteBuf
    init {
        val _numVRegs = abc.buf.readULeb128(offset)
        val _numArgs =abc.buf.readULeb128(_numVRegs.nextOffset)
        val _codeSize = abc.buf.readULeb128(_numArgs.nextOffset)
        numVRegs = _numVRegs.value
        numArgs = _numArgs.value
        codeSize = _codeSize.value
        _triesSize = abc.buf.readULeb128(_codeSize.nextOffset)
        instructions = abc.buf.slice(_triesSize.nextOffset, codeSize)
    }

    private val _tryBlocks:DataAndNextOff<List<TryBlock>> by lazy {
        var off = _triesSize.nextOffset + codeSize
        val list = ArrayList<TryBlock>(triesSize)
        repeat(triesSize){
            val tb = TryBlock(abc, off)
            list.add(tb)
            off = tb.nextOff
        }
        DataAndNextOff(list, off)
    }
    val tryBlocks:List<TryBlock> get() = _tryBlocks.value

    val asm by lazy { Asm(this) }

    override fun range(): OffsetRange = OffsetRange(offset, _tryBlocks.nextOffset)
    override val externalSize: Int get() = asm.list.fold(0) { s0,a -> s0 + a.literalArrays.fold(0) {s, l -> s + l.intrinsicSize} }
}