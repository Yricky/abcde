package me.yricky.oh.abcd.cfm

import me.yricky.oh.BaseOccupyRange
import me.yricky.oh.OffsetRange
import me.yricky.oh.SizeInBuf
import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.abcd.literal.ModuleLiteralArray
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value
import me.yricky.oh.utils.*
import kotlin.jvm.JvmInline

sealed class ClassItem(
    final override val abc: AbcBuf,
    final override val offset:Int
): AbcBufOffset {
    protected val nameItem get() = abc.stringItem(offset)
    val name by lazy {
        nameItem.value.removePrefix("L").removeSuffix(";")
    }
}

class ForeignClass(abc: AbcBuf, offset: Int) : ClassItem(abc, offset)
class AbcClass(abc: AbcBuf, offset: Int) : ClassItem(abc, offset), BaseOccupyRange,SizeInBuf.External{
    companion object{
        const val ENTRY_FUNC_NAME = "func_main_0"
    }

    val region get() = abc.regions.first { it.contains(offset) }

    private val superClassOff = abc.buf.getInt(nameItem.nextOffset)
    @Uncleared("reserved")
    val superClass get() = if(superClassOff != 0) abc.classes[superClassOff] else null

    val accessFlags:AccessFlags
    val numFields:Int

    private val _numMethods: IntAndNextOff
    val numMethods get() = _numMethods.value

    init {
        val _accessFlags = abc.buf.readULeb128(nameItem.nextOffset + 4)
        accessFlags = AccessFlags(_accessFlags.value)
        val _numFields = abc.buf.readULeb128(_accessFlags.nextOffset)
        numFields = _numFields.value
        _numMethods = abc.buf.readULeb128(_numFields.nextOffset)
    }

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
        fields.firstOrNull { it.isModuleRecordIdx() }?.getIntValue()
            ?.takeIf { abc.isValidOffset(it) }
            ?.let { ModuleLiteralArray(abc,it) }
    }
    val scopeNames:LiteralArray? by lazy {
        fields.firstOrNull { it.isScopeNames() }?.getIntValue()
            ?.takeIf { abc.isValidOffset(it) }
            ?.let { abc.literalArray(it) }
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

    override fun range(): OffsetRange = OffsetRange(offset, _methods.nextOffset)

    /**
     * TODO:尚不包含：
     * 1. [fields]中其他字段的额外体积
     * 2. 字节码汇编中引用的字面量数组等内容
     */
    override val externalSize: Int get() = methods.fold(0) { s, m -> s + m.externalSize } +
            (scopeNames?.intrinsicSize ?: 0) + (moduleInfo?.intrinsicSize ?: 0) +
            4 // class在abc的classIndex中存有一个u32类型的offset

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

fun AbcClass.entryFunction() = methods.firstOrNull { it.name == AbcClass.ENTRY_FUNC_NAME }

fun AbcClass.exportName():String? = moduleInfo?.let { mi ->
    if(mi.localExports.size == 1 && mi.indirectExports.isEmpty()){
        mi.localExports.first().exportName
    } else null
}

sealed class ClassTag{
    @Uncleared("最新文档中移除")
    sealed class AnnoTag(abc: AbcBuf, annoOffset:Int):ClassTag(){
        val anno:AbcAnnotation = AbcAnnotation(abc,annoOffset)

        override fun toString(): String {
            return "Annotation(${anno.clazz?.name}[${anno.elements}])"
        }
    }
    data object Nothing: ClassTag()
    @Uncleared("最新文档中移除")
    data class Interfaces(
        val count:Int,
        val indexInRegionList:List<Short>
    ): ClassTag()
    data class SourceLang(val value:Byte): ClassTag(){
        override fun toString(): String {
            return if(value == 0x0.toByte()) "SourceLang(ArkTS/TS/JS)" else "SourceLang($value)"
        }
    }
    @Uncleared("最新文档中移除")
    class RuntimeAnno(abc: AbcBuf, annoOffset:Int): AnnoTag(abc,annoOffset)
    @Uncleared("最新文档中移除")
    class Anno(abc: AbcBuf, annoOffset:Int): AnnoTag(abc, annoOffset)
    @Uncleared("最新文档中移除")
    class RuntimeTypeAnno(abc: AbcBuf, annoOffset:Int): AnnoTag(abc, annoOffset)
    @Uncleared("最新文档中移除")
    class TypeAnno(abc: AbcBuf, annoOffset:Int): AnnoTag(abc, annoOffset)
    data class SourceFile(val stringOffset:Int): ClassTag()

    companion object{
        fun readTag(abc: AbcBuf, offset: Int):DataAndNextOff<ClassTag>{
            val buf = abc.buf
            return when(val type = buf.get(offset).toInt()){
                0 -> Pair(Nothing,offset + 1)
                1 -> run{
                    val value = buf.readULeb128(offset + 1)
                    val count = value.value
                    val off = value.nextOffset
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