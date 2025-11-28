package me.yricky.oh.abcd.cfm

import me.yricky.oh.BaseOccupyRange
import me.yricky.oh.OffsetRange
import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.value

class AbcAnnotation(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset, BaseOccupyRange {
    private val region get() = abc.regions.first { it.contains(offset) }
    private val classIdx:UShort = abc.buf.getShort(offset).toUShort()
    val fieldType get() = region.classes[classIdx.toInt()]
    val clazz get() = fieldType.getClass(abc)
    val elementCount:UShort = abc.buf.getShort(offset+2).toUShort()
    val elements by lazy {
        val list = ArrayList<AnnotationElement>(elementCount.toInt())
        repeat(elementCount.toInt()){
            list.add(
                AnnotationElement(
                    nameOff = abc.buf.getInt(offset + 4 + 8 * it),
                    value = abc.buf.getInt(offset + 8 + 8 * it),
                    type = abc.buf.get(offset + 4 + 8 * elementCount.toInt() + it).let { Char(it.toUShort()) }
                )
            )
        }
        list
    }

    override fun range(): OffsetRange = OffsetRange.from(offset, 4 + elementCount.toInt() * 9)

    class AnnotationElement(
        val nameOff:Int,
        val value:Int,
        val type:Char
    ){
        fun name(abc: AbcBuf) = abc.stringItem(nameOff).value
        fun toString(abc: AbcBuf): String {
            return "${name(abc)}:${typeString(type)}=0x${value.toString(16)}"
        }

        companion object {
            fun typeString(type: Char):String {
                return when(type) {
                    '1' -> "u1"
                    '2' -> "i8"
                    '3' -> "u8"
                    '4' -> "i16"
                    '5' -> "u16"
                    '6' -> "i32"
                    '7' -> "u32"
                    '8' -> "i64"
                    '9' -> "u64"
                    'A' -> "f32"
                    'B' -> "f64"
                    'C' -> "string"
                    'E' -> "method"
                    'G' -> "annotation"
                    '#' -> "literalarray"
                    '0' -> "unknown"
                    else -> "unknown_${type}"
                }
            }
        }
    }

}