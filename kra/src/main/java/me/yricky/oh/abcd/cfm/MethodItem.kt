package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.code.DebugInfo
import me.yricky.oh.utils.*

sealed class MethodItem(
    val abc: AbcBuf,
    val offset:Int
){
    val region by lazy { abc.regions.first { it.contains(offset) } }

    private val classIdx:UShort = abc.buf.getShort(offset).toUShort()
    val clazz get() = region.classes[classIdx.toInt()]
    private val protoIdx:UShort = abc.buf.getShort(offset + 2).toUShort()
    val proto get() = region.protos[protoIdx.toInt()]
    private val nameOff:Int = abc.buf.getInt(offset + 4)

    val name :String get() = abc.stringItem(nameOff).value
//    init {
//        println("nOff:${name}")
//    }
    protected val _accessFlags by lazy {
        abc.buf.readULeb128(offset + 8)
    }
    val accessFlags get() = AbcMethod.AccessFlags(_accessFlags.value)
}
class ForeignMethod(abc: AbcBuf, offset: Int) : MethodItem(abc, offset)

class AbcMethod(abc: AbcBuf, offset: Int) :MethodItem(abc, offset){

    private val _data by lazy {
        var tagOff = _accessFlags.nextOffset
        val tagList = mutableListOf<MethodTag>()
        while (tagList.lastOrNull() != MethodTag.Nothing){
            val (tag,nextOff) = MethodTag.readTag(abc, tagOff)
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
    sealed class AnnoTag(abc: AbcBuf,annoOffset:Int):MethodTag(){
        val anno:AbcAnnotation = AbcAnnotation(abc,annoOffset)

        override fun toString(): String {
            return "Annotation(${anno.clazz.name})"
        }
    }
    sealed class ParamAnnoTag(val annoOffset:Int):MethodTag(){
        fun get(abc: AbcBuf): ParamAnnotation = ParamAnnotation(abc,annoOffset)
    }
    data object Nothing: MethodTag()
    data class Code(val offset:Int): MethodTag()
    data class SourceLang(val value:Byte): MethodTag()
    class RuntimeAnno(abc: AbcBuf,annoOffset: Int) : AnnoTag(abc,annoOffset)
    class RuntimeParamAnno(annoOffset: Int) :ParamAnnoTag(annoOffset)
    class DbgInfo(abc: AbcBuf,offset:Int): MethodTag(){
        val info = DebugInfo(abc,offset)

        override fun toString(): String {
            return "Dbg(lineStart=${info.lineStart},paramName=${info.params},cps=${info.constantPoolSize})"
        }
    }
    class Anno(abc: AbcBuf,annoOffset: Int) : AnnoTag(abc,annoOffset)
    class ParamAnno(annoOffset: Int) : ParamAnnoTag(annoOffset)
    class TypeAnno(abc: AbcBuf,annoOffset: Int) : AnnoTag(abc,annoOffset)
    class RuntimeTypeAnno(abc: AbcBuf,annoOffset: Int) : AnnoTag(abc,annoOffset)

    companion object{
        fun readTag(abc: AbcBuf, offset: Int):DataAndNextOff<MethodTag>{
            val buf = abc.buf
            return when(val type = buf.get(offset).toInt()){
                0 -> Pair(Nothing,offset + 1)
                1 -> run{
                    val value = buf.getInt(offset + 1)
                    Pair(Code(value), offset + 5)
                }
                2 -> Pair(SourceLang(buf.get(offset + 1)),offset + 2)
                3 -> Pair(RuntimeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                4 -> Pair(RuntimeParamAnno(buf.getInt(offset + 1)),offset + 5)
                5 -> Pair(DbgInfo(abc,buf.getInt(offset + 1)),offset + 5)
                6 -> Pair(Anno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                7 -> Pair(ParamAnno(buf.getInt(offset + 1)),offset + 5)
                8 -> Pair(TypeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                9 -> Pair(RuntimeTypeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                else -> throw IllegalStateException("No this Tag:${type.toString(16)}")
            }
        }
    }
}