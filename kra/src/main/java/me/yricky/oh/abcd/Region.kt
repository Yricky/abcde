package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.*

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

    //官方文档是这么写的，但是编译产物这里显然不对
//    val methods by lazy {
//        println("methodIdxSize:${header.methodIdxSize},off:${header.methodIdxOff.toString(16)}")
//        (0 until header.methodIdxSize).map {
//            val off = abc.buf.getInt(header.methodIdxOff + it * 4)
//            println("$it,off:${off.toString(16)}")
//            if (abc.isForeignOffset(off)){
//                println("foreign")
//                ForeignMethod(abc, off)
//            } else {
//                AbcMethod(abc, off)
//            }
//        }
//    }

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