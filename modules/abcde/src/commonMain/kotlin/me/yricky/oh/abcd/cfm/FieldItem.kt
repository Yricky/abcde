package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value
import me.yricky.oh.utils.*
import kotlin.jvm.JvmInline

sealed class FieldItem(
    final override val abc: AbcBuf,
    final override val offset:Int
): AbcBufOffset {
    val region by lazy { abc.regions.first { it.contains(offset) } }

    val classIdx:UShort = abc.buf.getShort(offset).toUShort()
    private val typeIdx:UShort = abc.buf.getShort(offset + 2).toUShort()
    val type:FieldType get() = region.classes[typeIdx.toInt()]
    private val nameOff:Int = abc.buf.getInt(offset + 4)
    val name :String get() = abc.stringItem(nameOff).value
    protected val _accessFlags = abc.buf.readULeb128(offset + 8)
}
class ForeignField(abc: AbcBuf, offset: Int): FieldItem(abc, offset)
class AbcField(abc: AbcBuf, offset: Int): FieldItem(abc, offset) {

    val accessFlags get() = AccessFlags(_accessFlags.value)
    private val _data by lazy {
        var tagOff = _accessFlags.nextOffset
        val tagList = mutableListOf<FieldTag>()
        while (tagList.lastOrNull() != FieldTag.Nothing){
            val (tag,nextOff) = FieldTag.readTag(abc, tagOff)
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

sealed class FieldTag{
    sealed class AnnoTag(abc: AbcBuf, annoOffset:Int):FieldTag(){
        val anno:AbcAnnotation = AbcAnnotation(abc,annoOffset)

        override fun toString(): String {
            return "Annotation(${anno.clazz.name})"
        }
    }
    data object Nothing: FieldTag()
    data class IntValue(val value:Int): FieldTag()
    data class Value(val value:Int): FieldTag()
    class RuntimeAnno(abc: AbcBuf, annoOffset: Int): AnnoTag(abc,annoOffset)
    class Anno(abc: AbcBuf, annoOffset: Int): AnnoTag(abc, annoOffset)
    class RuntimeTypeAnno(abc: AbcBuf, annoOffset: Int): AnnoTag(abc, annoOffset)
    class TypeAnno(abc: AbcBuf, annoOffset: Int): AnnoTag(abc, annoOffset)

    companion object{
        fun readTag(abc: AbcBuf, offset: Int):DataAndNextOff<FieldTag>{
            val buf = abc.buf
            return when(val type = buf.get(offset).toInt()){
                0 -> Pair(Nothing,offset + 1)
                1 -> run{
                    val (value,off) = buf.readULeb128(offset + 1)
                    Pair(IntValue(value), off)
                }
                2 -> Pair(Value(buf.getInt(offset + 1)),offset + 5)
                3 -> Pair(RuntimeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                4 -> Pair(Anno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                5 -> Pair(RuntimeTypeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                6 -> Pair(TypeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                else -> throw IllegalStateException("No this Tag:${type.toString(16)}")
            }
        }
    }
}

fun AbcField.isModuleRecordIdx() :Boolean = type.name == "u32" && name == "moduleRecordIdx"
fun AbcField.isScopeNames() :Boolean = type.name == "u32" && name == "scopeNames"
fun AbcField.getIntValue():Int? = run {
    (data.firstOrNull { it is FieldTag.IntValue } as? FieldTag.IntValue)?.value
} ?: run {
    (data.firstOrNull { it is FieldTag.Value } as? FieldTag.Value)?.value
}