package me.yricky.oh.abcd.code

import me.yricky.oh.BaseOccupyRange
import me.yricky.oh.OffsetRange
import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value
import me.yricky.oh.common.LazyMapList
import me.yricky.oh.utils.readULeb128

class DebugInfo(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset, BaseOccupyRange {
    private val _lineStart = abc.buf.readULeb128(offset)
    val lineStart get() = _lineStart.value

    private val _numParams = abc.buf.readULeb128(_lineStart.nextOffset)

    private val _params: DataAndNextOff<List<Int>> by lazy {
        if(_numParams.value == 0){
            DataAndNextOff(emptyList(),_numParams.nextOffset)
        } else {
            val list = ArrayList<Int>(_numParams.value)
            var off = _numParams.nextOffset
            repeat(_numParams.value){
                val strOff = abc.buf.readULeb128(off)
                if (strOff.value != 0){
                    list.add(strOff.value)
                } else {
                    list.add(0)
                }
                off = strOff.nextOffset
            }
            DataAndNextOff(list,off)
        }
    }
    val paramsNameOff get() = _params.value
    val params:List<String> get() = if(paramsNameOff.isEmpty()) emptyList() else LazyMapList(paramsNameOff) { if(it == 0) "" else abc.stringItem(it).value }

    private val _constantPoolSize by lazy {
        abc.buf.readULeb128(_params.nextOffset)
    }
    private val constantPoolSize get() = _constantPoolSize.value
    private val _constantPool by lazy {
        val list = ArrayList<Int>()
        val initOff = _constantPoolSize.nextOffset
        var off = _constantPoolSize.nextOffset
        while (off - initOff < constantPoolSize){
            val intOff = abc.buf.readULeb128(off)
            list.add(intOff.value)
            off = intOff.nextOffset
        }
        DataAndNextOff(list,off)
    }
    val constantPool:List<Int> get() = _constantPool.value

    private val _lineNumberProgramIdx get() = abc.buf.readULeb128(_constantPoolSize.nextOffset + constantPoolSize)
    private val lineNumberProgramIdx get() = _lineNumberProgramIdx.value
    val lineNumberProgram get() = abc.lnps.getOrNull(lineNumberProgramIdx)

    override fun range(): OffsetRange = OffsetRange(offset, _lineNumberProgramIdx.nextOffset)
}