package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.utils.Uncleared

class Region(
    private val abc:AbcBuf,
    private val offset:Int
) {
    val header by lazy { RegionHeader() }

    fun contains(offset:Int):Boolean = offset in header.startOff until header.endOff

    override fun toString(): String {
        return "R[${header.startOff},${header.endOff})@${offset.toString(16)}"
    }

    val classes by lazy {
        (0 until header.classIdxSize).map {
            FieldType.fromOffset(abc,abc.buf.getInt(header.classIdxOff + it * 4))
        }
    }

    val mslIndex by lazy {
        println("methodIdxSize:${header.mslIdxSize},off:${header.mslIdxOff.toString(16)}")
        (0 until header.mslIdxSize).map {
            abc.buf.getInt(header.mslIdxOff + it * 4)
        }
    }

    @Uncleared("reserved")
    val fields by lazy {
        (0 until header.fieldIdxSize).map {
            val off = abc.buf.getInt(header.fieldIdxOff + it * 4)
            if (abc.isForeignOffset(off)){
//                println("foreign")
                ForeignField(abc, off)
            } else {
                AbcField(abc, off)
            }
        }
    }

    @Uncleared("reserved")
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
        val mslIdxSize = abc.buf.getInt(offset + 16)
        val mslIdxOff = abc.buf.getInt(offset + 20)
        @Uncleared("reserved")
        val fieldIdxSize = abc.buf.getInt(offset + 24)
        @Uncleared("reserved")
        val fieldIdxOff = abc.buf.getInt(offset + 28)
        @Uncleared("reserved")
        val protoIdxSize = abc.buf.getInt(offset + 32)
        @Uncleared("reserved")
        val protoIdxOff = abc.buf.getInt(offset + 36)
    }
}