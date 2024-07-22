package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value
import me.yricky.oh.utils.readULeb128

class TryBlock(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset {
    private val _startPc = abc.buf.readULeb128(offset)
    val startPc:Int get() = _startPc.value

    private val _length = abc.buf.readULeb128(_startPc.nextOffset)
    val length:Int get() = _length.value

    private val _numCatches = abc.buf.readULeb128(_length.nextOffset)
    val numCatches:Int get() = _numCatches.value

    private val _catchBlocks by lazy {
        var off = _numCatches.nextOffset
        val list = ArrayList<CatchBlock>(_numCatches.value)
        repeat(_numCatches.value){
            val _tIdx = abc.buf.readULeb128(off)
            val _hPc = abc.buf.readULeb128(_tIdx.nextOffset)
            val _cs = abc.buf.readULeb128(_hPc.nextOffset)
            off = _cs.nextOffset
            list.add(CatchBlock(_tIdx.value,_hPc.value,_cs.value))
        }
        DataAndNextOff(list,off)
    }
    val catchBlocks get() = _catchBlocks.value

    val nextOff get() = _catchBlocks.nextOffset

    data class CatchBlock(
        val typeIdx:Int,
        val handlerPc:Int,
        val codeSize:Int
    )
}