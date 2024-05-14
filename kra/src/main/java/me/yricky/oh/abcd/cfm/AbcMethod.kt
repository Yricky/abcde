package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.utils.*
import java.nio.ByteBuffer

class AbcMethod(
    val abc: AbcBuf,
    val offset:Int
) {
    val region by lazy { abc.regions.first { it.contains(offset) } }

    private val classIdx:UShort = abc.buf.getShort(offset).toUShort()
    val clazz get() = region.classes[classIdx.toInt()]
    private val protoIdx:UShort = abc.buf.getShort(offset + 2).toUShort()
    val proto get() = region.protos[protoIdx.toInt()]
    private val nameOff:Int = abc.buf.getInt(offset + 4)
    val name :String get() = abc.stringItem(nameOff).value
    private val _accessFlags by lazy {
        abc.buf.readULeb128(offset + 8)
    }
    val accessFlags get() = AccessFlags(_accessFlags.value)
    private val _data by lazy {
        var tagOff = _accessFlags.nextOffset
        val tagList = mutableListOf<MethodTag>()
        while (tagList.lastOrNull() != MethodTag.Nothing){
            val (tag,nextOff) = MethodTag.readTag(abc.buf, tagOff)
            tagList.add(tag)
            tagOff = nextOff
        }
        if(tagList.lastOrNull() == MethodTag.Nothing){
            tagList.removeLast()
        }
        DataAndNextOff(tagList,tagOff)
    }
    val data:List<MethodTag> get() = _data.value

    val codeItem: Code? by lazy {
        data.firstOrNull { it is MethodTag.Code }?.let { Code(abc,(it as MethodTag.Code).offset) }
    }

    @JvmInline
    value class AccessFlags(private val value:Int){
        val isPublic:Boolean get() = (value and 0x0001) != 0
        val isPrivate:Boolean get() = (value and 0x0002) != 0
        val isProtected:Boolean get() = (value and 0x0004) != 0
        val isStatic:Boolean get() = (value and 0x0008) != 0
        val isFinal:Boolean get() = (value and 0x0010) != 0
        val isSynchronized:Boolean get() = (value and 0x0020) != 0
        val isNative:Boolean get() = (value and 0x0100) != 0
        val isAbstract:Boolean get() = (value and 0x0400) != 0
        val isSynthetic:Boolean get() = (value and 0x1000) != 0
    }

    val nextOff get() = _data.nextOffset
}

sealed class MethodTag{
    sealed class AnnoTag(val annoOffset:Int):MethodTag(){
        fun get(abc: AbcBuf):AbcAnnotation = AbcAnnotation(abc,annoOffset)
    }
    sealed class ParamAnnoTag(val annoOffset:Int):MethodTag(){
        fun get(abc: AbcBuf): ParamAnnotation = ParamAnnotation(abc,annoOffset)
    }
    data object Nothing: MethodTag()
    data class Code(val offset:Int): MethodTag()
    data class SourceLang(val value:Byte): MethodTag()
    class RuntimeAnno(annoOffset: Int) : AnnoTag(annoOffset)
    class RuntimeParamAnno(annoOffset: Int) :ParamAnnoTag(annoOffset)
    data class DebugInfo(val offset:Int): MethodTag()
    class Anno(annoOffset: Int) : AnnoTag(annoOffset)
    class ParamAnno(annoOffset: Int) : ParamAnnoTag(annoOffset)
    class TypeAnno(annoOffset: Int) : AnnoTag(annoOffset)
    class RuntimeTypeAnno(annoOffset: Int) : AnnoTag(annoOffset)

    companion object{
        fun readTag(buf: ByteBuffer, offset: Int):DataAndNextOff<MethodTag>{
            return when(val type = buf.get(offset).toInt()){
                0 -> Pair(Nothing,offset + 1)
                1 -> run{
                    val value = buf.getInt(offset + 1)
                    Pair(Code(value), offset + 5)
                }
                2 -> Pair(SourceLang(buf.get(offset + 1)),offset + 2)
                3 -> Pair(RuntimeAnno(buf.getInt(offset + 1)),offset + 5)
                4 -> Pair(RuntimeParamAnno(buf.getInt(offset + 1)),offset + 5)
                5 -> Pair(DebugInfo(buf.getInt(offset + 1)),offset + 5)
                6 -> Pair(Anno(buf.getInt(offset + 1)),offset + 5)
                7 -> Pair(ParamAnno(buf.getInt(offset + 1)),offset + 5)
                8 -> Pair(TypeAnno(buf.getInt(offset + 1)),offset + 5)
                9 -> Pair(RuntimeTypeAnno(buf.getInt(offset + 1)),offset + 5)
                else -> throw IllegalStateException("No this Tag:${type.toString(16)}")
            }
        }
    }
}