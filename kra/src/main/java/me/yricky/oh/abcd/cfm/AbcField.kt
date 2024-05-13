package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.utils.*
import java.nio.ByteBuffer

class AbcField(
    val abc: AbcBuf,
    val offset:Int
) {
    val region by lazy { abc.regions.first { it.contains(offset) } }

    val classIdx:UShort = abc.buf.getShort(offset).toUShort()
    private val typeIdx:UShort = abc.buf.getShort(offset + 2).toUShort()
    val type:FieldType get() = region.classes[typeIdx.toInt()]
    private val nameOff:Int = abc.buf.getInt(offset + 4)
    val name :String by lazy { abc.buf.stringItem(nameOff).value }
    private val _accessFlags by lazy {
        abc.buf.readULeb128(offset + 8)
    }
    val accessFlags get() = AccessFlags(_accessFlags.value)
    private val _data by lazy {
        var tagOff = _accessFlags.nextOffset
        val tagList = mutableListOf<FieldTag>()
        while (tagList.lastOrNull() != FieldTag.Nothing){
            val (tag,nextOff) = FieldTag.readTag(abc.buf, tagOff)
            tagList.add(tag)
            tagOff = nextOff
        }
        if(tagList.lastOrNull() == FieldTag.Nothing){
            tagList.removeLast()
        }
        DataAndNextOff(tagList,tagOff)
    }
    val data:List<FieldTag> get() = _data.value

    @JvmInline
    value class AccessFlags(private val value:Int){
        val isPublic:Boolean get() = (value and 0x0001) != 0
        val isPrivate:Boolean get() = (value and 0x0002) != 0
        val isProtected:Boolean get() = (value and 0x0004) != 0
        val isStatic:Boolean get() = (value and 0x0008) != 0
        val isFinal:Boolean get() = (value and 0x0010) != 0
        val isVolatile:Boolean get() = (value and 0x0040) != 0

        val isSynthetic:Boolean get() = (value and 0x1000) != 0
        val isEnum:Boolean get() = (value and 0x4000) != 0
    }

    val nextOff get() = _data.nextOffset
}

sealed class FieldTag(tag:Byte){
    data object Nothing: FieldTag(0)
    data class IntValue(val value:Int): FieldTag(1)
    data class Value(val value:Int): FieldTag(2)
    data class RuntimeAnnotation(val annoOffset:Int): FieldTag(3)
    data class Annotation(val annoOffset:Int): FieldTag(4)
    data class RuntimeTypeAnnotation(val annoOffset:Int): FieldTag(5)
    data class TypeAnnotation(val annoOffset:Int): FieldTag(6)

    companion object{
        fun readTag(buf: ByteBuffer, offset: Int):DataAndNextOff<FieldTag>{
            return when(val type = buf.get(offset).toInt()){
                0 -> Pair(Nothing,offset + 1)
                1 -> run{
                    val (value,off) = buf.readULeb128(offset + 1)
                    Pair(IntValue(value), off)
                }
                2 -> Pair(Value(buf.getInt(offset + 1)),offset + 5)
                3 -> Pair(RuntimeAnnotation(buf.getInt(offset + 1)),offset + 5)
                4 -> Pair(Annotation(buf.getInt(offset + 1)),offset + 5)
                5 -> Pair(RuntimeTypeAnnotation(buf.getInt(offset + 1)),offset + 5)
                6 -> Pair(TypeAnnotation(buf.getInt(offset + 1)),offset + 5)
                else -> throw IllegalStateException("No this Tag:${type.toString(16)}")
            }
        }
    }
}

fun AbcField.isModuleRecordIdx() :Boolean = type.name == "u32" && name != "typeSummaryOffset"
fun AbcField.getIntValue():Int? = run {
    (data.firstOrNull { it is FieldTag.IntValue } as? FieldTag.IntValue)?.value
} ?: run {
    (data.firstOrNull { it is FieldTag.Value } as? FieldTag.Value)?.value
}