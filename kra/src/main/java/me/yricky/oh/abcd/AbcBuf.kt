package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.cfm.ForeignClass
import me.yricky.oh.abcd.literal.LiteralArray
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
}