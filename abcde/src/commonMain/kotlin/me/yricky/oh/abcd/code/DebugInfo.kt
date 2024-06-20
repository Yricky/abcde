package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.utils.DataAndNextOff
import me.yricky.oh.utils.nextOffset
import me.yricky.oh.utils.readULeb128
import me.yricky.oh.utils.value

class DebugInfo(
    val abc: me.yricky.oh.abcd.AbcBuf,
    val offset:Int
) {
    private val _lineStart by lazy {
        abc.buf.readULeb128(offset)
    }
    val lineStart get() = _lineStart.value

    private val _numParams by lazy {
        abc.buf.readULeb128(_lineStart.nextOffset)
    }

    private val _params by lazy {
        val list = ArrayList<String>(_numParams.value)
        var off = _numParams.nextOffset
        repeat(_numParams.value){
            val strOff = abc.buf.readULeb128(off)
            if (strOff.value != 0){
                list.add(abc.stringItem(strOff.value).value)
            } else {
                list.add("")
            }
            off = strOff.nextOffset
        }
        DataAndNextOff(list,off)
    }
    val params:List<String> get() = _params.value

    private val _constantPoolSize by lazy {
        abc.buf.readULeb128(_params.nextOffset)
    }
    val constantPoolSize get() = _constantPoolSize.value
}