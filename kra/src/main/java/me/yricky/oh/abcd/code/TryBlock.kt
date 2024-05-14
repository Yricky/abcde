package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.utils.DataAndNextOff
import me.yricky.oh.utils.nextOffset
import me.yricky.oh.utils.readULeb128
import me.yricky.oh.utils.value

class TryBlock(
    val abc: AbcBuf,
    val offset:Int
) {
    private val _startPc by lazy {
        abc.buf.readULeb128(offset)
    }
    val startPc = _startPc.value

    private val _length by lazy {
        abc.buf.readULeb128(_startPc.nextOffset)
    }
    val length = _length.value

    private val _numCatches by lazy {
        abc.buf.readULeb128(_length.nextOffset)
    }
    val numCatches = _numCatches.value

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