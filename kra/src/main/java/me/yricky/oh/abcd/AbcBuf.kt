package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.abcd.literal.ModuleLiteralArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AbcBuf(
    _buf:ByteBuffer
) {
    val buf: ByteBuffer = _buf.order(ByteOrder.LITTLE_ENDIAN)
    val header = AbcHeader(buf)
    val classes by lazy {
        (0 until header.numClasses).associate { i ->
            val classIndex = buf.getInt(header.classIdxOff + i * 4)
            Pair(
                classIndex,
                if(classIndex in header.foreignOff until (header.foreignOff + header.foreignSize)){
                    ForeignClass(this,classIndex)
                } else {
                    ClassItem(this,classIndex)
                }
            )
        }
    }

    val regions by lazy {
        (0 until header.numIndexRegions).map {
            Region(this,header.indexSectionOff + it * 40)
        }
    }

    val literalArrays by lazy {
        (0 until header.numLiteralArrays).map {
            LiteralArray(this,buf.getInt(header.literalArrayIdxOff + it * 4))
        }
    }

    val moduleLiteralArrays by lazy {
        val map = LinkedHashMap<Int,ModuleLiteralArray>()
        classes.forEach { (i, c) ->
            if(c is ClassItem){
                c.fields.firstOrNull { it.isModuleRecordIdx() }?.getIntValue()
                    ?.takeIf { isValidOffset(it) }
                    ?.let { map[it] = ModuleLiteralArray(this,it) }
            }
        }
        map
    }

    fun isValidOffset(offset:Int): Boolean{
        return offset >= 60 && offset < buf.limit()
    }
}