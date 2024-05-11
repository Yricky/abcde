package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.FieldType
import me.yricky.oh.abcd.cfm.Proto

class Region(
    private val abc:AbcBuf,
    private val offset:Int
) {
    val header by lazy { RegionHeader() }

    fun contains(offset:Int):Boolean = offset in header.startOff until header.endOff

    override fun toString(): String {
        return "R[${header.startOff},${header.endOff})"
    }

    val classes by lazy {
        (0 until header.classIdxSize).map {
            FieldType.fromOffset(abc,abc.buf.getInt(header.classIdxOff + it * 4))
        }
    }

    val protos by lazy {
        (0 until header.protoIdxSize).map {
            Proto(abc,abc.buf.getInt(header.protoIdxOff + it * 4))
        }
    }

    inner class RegionHeader{
        val startOff = abc.buf.getInt(offset)
        val endOff = abc.buf.getInt(offset + 4)
        val classIdxSize = abc.buf.getInt(offset + 8)
        val classIdxOff = abc.buf.getInt(offset + 12)
        val methodIdxSize = abc.buf.getInt(offset + 16)
        val methodIdxOff = abc.buf.getInt(offset + 20)
        val fieldIdxSize = abc.buf.getInt(offset + 24)
        val fieldIdxOff = abc.buf.getInt(offset + 28)
        val protoIdxSize = abc.buf.getInt(offset + 32)
        val protoIdxOff = abc.buf.getInt(offset + 36)
    }
}