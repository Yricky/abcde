package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.value

class AbcAnnotation(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset {
    private val region by lazy { abc.regions.first { it.contains(offset) } }
    private val classIdx:UShort = abc.buf.getShort(offset).toUShort()
    val clazz get() = region.classes[classIdx.toInt()]
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

    class AnnotationElement(
        val nameOff:Int,
        val value:Int,
        val type:Char
    ){
        fun name(abc: AbcBuf) = abc.stringItem(nameOff).value
        fun toString(abc: AbcBuf): String {
            return "${name(abc)}:${type}=${value.toString(16)}"
        }
    }
}