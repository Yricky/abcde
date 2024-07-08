package me.yricky.oh.abcd

import me.yricky.oh.common.LEByteBuf
import me.yricky.oh.abcd.cfm.*

class Region(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset {
    override val buf: LEByteBuf get() = abc.buf
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
//        println("methodIdxSize:${header.mslIdxSize},off:${header.mslIdxOff.toString(16)}")
        (0 until header.mslIdxSize).map {
            abc.buf.getInt(header.mslIdxOff + it * 4)
        }
    }

//    /**
//     * 在字节码12.0.1.0版本后始终为空
//     */
//    @Deprecated("since 12.0.1.0", level = DeprecationLevel.HIDDEN)
//    val fields by lazy {
//        (0 until header.fieldIdxSize).map {
//            val off = abc.buf.getInt(header.fieldIdxOff + it * 4)
//            if (abc.isForeignOffset(off)){
//                println("foreign")
//                ForeignField(abc, off)
//            } else {
//                AbcField(abc, off)
//            }
//        }
//    }

    /**
     * 在字节码12.0.1.0版本后始终为空
     */
    @Deprecated("since 12.0.1.0")
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
//        @Deprecated("since 12.0.1.0")
//        val fieldIdxSize = abc.buf.getInt(offset + 24)
//        @Deprecated("since 12.0.1.0")
//        val fieldIdxOff = abc.buf.getInt(offset + 28)
        @Deprecated("since 12.0.1.0")
        val protoIdxSize = abc.buf.getInt(offset + 32)
        @Deprecated("since 12.0.1.0")
        val protoIdxOff = abc.buf.getInt(offset + 36)
    }
}