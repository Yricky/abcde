package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.literal.ModuleLiteralArray
import me.yricky.oh.utils.*
import kotlin.jvm.JvmInline

sealed class ClassItem(
    val abc: me.yricky.oh.abcd.AbcBuf,
    val offset:Int
) {
    protected val nameItem = abc.stringItem(offset)
    val name by lazy {
        nameItem.value.removePrefix("L").removeSuffix(";")
    }
}

class ForeignClass(abc: me.yricky.oh.abcd.AbcBuf, offset: Int) : ClassItem(abc, offset)
class AbcClass(abc: me.yricky.oh.abcd.AbcBuf, offset: Int) : ClassItem(abc, offset){
    val region by lazy { abc.regions.first { it.contains(offset) } }

    private val superClassOff by lazy { abc.buf.getInt(nameItem.nextOffset) }
    @Uncleared("reserved")
    val superClass get() = if(superClassOff != 0) abc.classes[superClassOff] else null

    private val _accessFlags by lazy { abc.buf.readULeb128(nameItem.nextOffset + 4) }
    val accessFlags get() = AccessFlags(_accessFlags.value)

    private val _numFields by lazy { abc.buf.readULeb128(_accessFlags.nextOffset) }
    val numFields get() = _numFields.value

    private val _numMethods by lazy { abc.buf.readULeb128(_numFields.nextOffset) }
    val numMethods get() = _numMethods.value

    private val _data by lazy {
        var tagOff = _numMethods.nextOffset
        val tagList = mutableListOf<ClassTag>()
        while (tagList.lastOrNull() != ClassTag.Nothing){
            val (tag,nextOff) = ClassTag.readTag(abc, tagOff)
            tagList.add(tag)
            tagOff = nextOff
        }
        if(tagList.lastOrNull() == ClassTag.Nothing){
            tagList.removeLast()
        }
        DataAndNextOff(tagList,tagOff)
    }
    val data:List<ClassTag> get() = _data.value

    private val _fields by lazy {
        val list = ArrayList<AbcField>(numFields)
        var off = _data.nextOffset
        repeat(numFields){
            list.add(AbcField(abc,off))
            off = list.last().nextOff
        }
        DataAndNextOff(list,off)
    }
    val fields:List<AbcField> get() = _fields.value
    val moduleInfo:ModuleLiteralArray? by lazy {
        fields.firstOrNull { it.isModuleRecordIdx() }?.let { abc.moduleLiteralArrays[it.getIntValue()] }
    }

    private val _methods by lazy {
        val list = ArrayList<AbcMethod>(numMethods)
        var off = _fields.nextOffset
        repeat(numMethods){
            list.add(abc.method(off) as AbcMethod)
            off = list.last().nextOff
        }
        DataAndNextOff(list,off)
    }
    val methods:List<AbcMethod> get() = _methods.value

    @JvmInline
    value class AccessFlags(private val value:Int){
        val isPublic:Boolean get() = (value and 0x0001) != 0
        val isFinal:Boolean get() = (value and 0x0010) != 0
        val isInterface:Boolean get() = (value and 0x0200) != 0
        val isAbstract:Boolean get() = (value and 0x0400) != 0
        val isSynthetic:Boolean get() = (value and 0x1000) != 0
        val isAnnotation:Boolean get() = (value and 0x2000) != 0
        val isEnum:Boolean get() = (value and 0x4000) != 0
    }

}

sealed class ClassTag{
    sealed class AnnoTag(abc: me.yricky.oh.abcd.AbcBuf, annoOffset:Int):ClassTag(){
        val anno:AbcAnnotation = AbcAnnotation(abc,annoOffset)

        override fun toString(): String {
            return "Annotation(${anno.clazz.name})"
        }
    }
    data object Nothing: ClassTag()
    data class Interfaces(
        val count:Int,
        val indexInRegionList:List<Short>
    ): ClassTag()
    data class SourceLang(val value:Byte): ClassTag()
    class RuntimeAnno(abc: me.yricky.oh.abcd.AbcBuf, annoOffset:Int): AnnoTag(abc,annoOffset)
    class Anno(abc: me.yricky.oh.abcd.AbcBuf, annoOffset:Int): AnnoTag(abc, annoOffset)
    class RuntimeTypeAnno(abc: me.yricky.oh.abcd.AbcBuf, annoOffset:Int): AnnoTag(abc, annoOffset)
    class TypeAnno(abc: me.yricky.oh.abcd.AbcBuf, annoOffset:Int): AnnoTag(abc, annoOffset)
    data class SourceFile(val stringOffset:Int): ClassTag()

    companion object{
        fun readTag(abc: me.yricky.oh.abcd.AbcBuf, offset: Int):DataAndNextOff<ClassTag>{
            val buf = abc.buf
            return when(val type = buf.get(offset).toInt()){
                0 -> Pair(Nothing,offset + 1)
                1 -> run{
                    val (count,off) = buf.readULeb128(offset + 1)
                    val list = (0 until count).map {
                        buf.getShort(off + it * 2)
                    }
                    Pair(Interfaces(count,list), off + count * 2)
                }
                2 -> Pair(SourceLang(buf.get(offset + 1)), offset + 2)
                3 -> Pair(RuntimeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                4 -> Pair(Anno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                5 -> Pair(RuntimeTypeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                6 -> Pair(TypeAnno(abc,abc.buf.getInt(offset + 1)),offset + 5)
                7 -> Pair(SourceFile(buf.getInt(offset + 1)),offset + 5)
                else -> throw IllegalStateException("No this Tag:${type.toString(16)}")
            }
        }
    }
}