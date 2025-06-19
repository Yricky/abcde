package me.yricky.oh.abcd.analyze

import me.yricky.oh.SizeInBuf
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.isa.literalArrays
import me.yricky.oh.abcd.isa.stringIds
import me.yricky.oh.abcd.literal.LiteralArray


@JvmInline
value class TaggedSize(val raw:Long){
    val value:Int get() = (raw and 0x7fff_ffffL).toInt()
    val tag:Long get() = raw.ushr(32)

    fun isSize() = raw.ushr(63) == 0L

    companion object{
        const val INVALID = -1L
        //
        const val SIZE_UNTAGGED = 0L
        const val SIZE_CLZ_INTRINSIC = 0x1L // 类本身的固有尺寸
        const val SIZE_CODE_INTRINSIC = 0x2L // 方法内容段本身的固有尺寸
        const val SIZE_MLA_INTRINSIC = 0x3L // 类导入导出信息的固有尺寸
        const val SIZE_LA_INTRINSIC = 0x4L // 类导入导出信息的固有尺寸
        const val SIZE_LNP_INTRINSIC = 0x5L
        const val OFF32_STRING = 0x8000_0000L
        const val OFF32_LA = 0x8000_0001L
        const val OFF32_METHOD = 0x8000_0002L
    }
}

fun TaggedSize(tag: Long,value:Int) = TaggedSize(tag.shl(32) or value.toLong())

fun yieldSize(clazz: AbcClass): Sequence<TaggedSize> = sequence {
    yield(TaggedSize(TaggedSize.SIZE_CLZ_INTRINSIC, clazz.intrinsicSize))
    yield(TaggedSize(TaggedSize.SIZE_UNTAGGED, 4))
    clazz.moduleInfo?.let {
        yield(TaggedSize(TaggedSize.SIZE_MLA_INTRINSIC, it.intrinsicSize))
        it.moduleRequestStrOffs.forEach {
            yield(TaggedSize(TaggedSize.OFF32_STRING,it))
        }

    }
    clazz.scopeNames?.let {
        yield(TaggedSize(TaggedSize.OFF32_LA, it.offset))
    }
    clazz.fields.forEach {
        yield(TaggedSize(TaggedSize.OFF32_STRING,it.nameOff))
    }
    clazz.methods.forEach {
        yield(TaggedSize(TaggedSize.OFF32_METHOD, it.offset))
    }
}

fun yieldSize(method: AbcMethod): Sequence<TaggedSize> = sequence {
    yield(TaggedSize(TaggedSize.OFF32_STRING, method.nameOff))
    method.data.forEach {
        if(it is SizeInBuf.External){
            yield(TaggedSize(TaggedSize.SIZE_UNTAGGED,it.externalSize))
        }
    }
    method.debugInfo?.let {
        // abc文件中的lnps中存有一个u32类型的offset)
        yield(TaggedSize(TaggedSize.SIZE_UNTAGGED, it.info.intrinsicSize + 4))
        it.state?.let {
            yield(TaggedSize(TaggedSize.SIZE_UNTAGGED, it.lnpSize))
            it.fileStringOff?.let {
                yield(TaggedSize(TaggedSize.OFF32_STRING, it))
            }
            it.sourceCodeStringOff?.let {
                yield(TaggedSize(TaggedSize.OFF32_STRING, it))
            }
        }
    }
    method.codeItem?.let {
        yield(TaggedSize(TaggedSize.SIZE_CODE_INTRINSIC, it.intrinsicSize))
        it.asm.list.forEach { item ->
            item.stringIds.forEach {
                yield(TaggedSize(TaggedSize.OFF32_STRING,it))
            }
            item.literalArrays.forEach {
                yield(TaggedSize(TaggedSize.OFF32_LA, it.offset))
            }
        }
    }

}

fun yieldSize(la: LiteralArray): Sequence<TaggedSize> = sequence {
    yield(TaggedSize(TaggedSize.SIZE_LA_INTRINSIC, la.intrinsicSize))
    la.content.forEach {
        if(it is LiteralArray.Literal.Str){
            yield(TaggedSize(TaggedSize.OFF32_STRING, it.offset))
        }
    }
}

