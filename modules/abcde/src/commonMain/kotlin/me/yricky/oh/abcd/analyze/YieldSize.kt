package me.yricky.oh.abcd.analyze

import me.yricky.oh.BaseOccupyRange
import me.yricky.oh.OffsetRange
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcAnnotation
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.ClassTag
import me.yricky.oh.abcd.cfm.MethodTag
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.code.DebugInfo
import me.yricky.oh.abcd.code.LineNumberProgram
import me.yricky.oh.abcd.code.SetFile
import me.yricky.oh.abcd.code.StartLocal
import me.yricky.oh.abcd.code.StartLocalExt
import me.yricky.oh.abcd.isa.literalArrays
import me.yricky.oh.abcd.isa.stringIds
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.abcd.literal.ModuleLiteralArray



sealed interface TaggedRange: BaseOccupyRange{
    val tag:String
    sealed interface ReusableTaggedRange: TaggedRange

    /**
     * 这段区间内引用的其他区间，不包括这段区间本身。
     */
    fun externalRanges(): Sequence<TaggedRange> = emptySequence()

    class Clz(val clazz: AbcClass): TaggedRange, BaseOccupyRange by clazz {
        override val tag: String get() = "clz"

        override fun externalRanges(): Sequence<TaggedRange> = sequence {
            clazz.moduleInfo?.let { yield(MLA(it)) }
            clazz.scopeNames?.let { yield(LA(it)) }
            clazz.data.forEach {
                if(it is ClassTag.SourceFile){
                    yield(Str(clazz.abc, it.stringOffset))
                }
            }
            clazz.fields.forEach {
                yield(Str(clazz.abc, it.nameOff))
            }
            clazz.methods.forEach {
                yield(Str(clazz.abc, it.nameOff))
                it.data.forEach { tag ->
                    if(tag is MethodTag.AnnoTag) {
                        yield(Anno(tag.anno))
                    }
                }
                it.debugInfo?.let { yield(Dbg(it.info)) }
                it.codeItem?.let { yield(MethodCode(it)) }
            }
        }
    }

    class Anno(val anno: AbcAnnotation): TaggedRange, BaseOccupyRange by anno {
        override val tag: String get() = "anno"
        override fun externalRanges(): Sequence<TaggedRange> = sequence {
            anno.elements.forEach {
                yield(Str(anno.abc, it.nameOff))
            }
        }
    }

    class MethodCode(val code: Code): TaggedRange, BaseOccupyRange by code {
        override val tag: String get() = "code"
        override fun externalRanges(): Sequence<TaggedRange> = sequence {
            code.asm.list.forEach { item ->
                item.stringIds.forEach {
                    yield(Str(code.abc, it))
                }
                item.literalArrays.forEach {
                    yield(LA(code.abc.literalArray(it.offset)))
                }
            }
        }
    }

    class LA(val la: LiteralArray): TaggedRange, BaseOccupyRange by la {
        override val tag: String get() = "la"
        override fun externalRanges(): Sequence<TaggedRange> = sequence {
            la.content.forEach {
                if(it is LiteralArray.Literal.Str){
                    yield(Str(la.abc, it.offset))
                } else if(it is LiteralArray.Literal.LiteralArr){
                    yield(LA(la.abc.literalArray(it.offset)))
                } else if(it is LiteralArray.Literal.LiteralRef && it !is LiteralArray.Literal.LiteralMethod) {
                    println(it)
                }
            }
        }
    }

    class MLA(val mla: ModuleLiteralArray): TaggedRange, BaseOccupyRange by mla {
        override val tag: String get() = "mla"
        override fun externalRanges(): Sequence<TaggedRange> = sequence {
            mla.moduleRequestStrOffs.forEach {
                yield(Str(mla.abc, it))
            }
        }
    }

    class Str(val abc: AbcBuf, val offset:Int): ReusableTaggedRange {
        override val tag: String get() = "str"
        override fun range(): OffsetRange = OffsetRange(offset, abc.stringItem(offset).second)
    }

    class Dbg(val dbg: DebugInfo): ReusableTaggedRange, BaseOccupyRange by dbg {
        override val tag: String get() = "dbg"
        override fun externalRanges(): Sequence<TaggedRange> = sequence {
            dbg.paramsNameOff.forEach {
                yield(Str(dbg.abc, it))
            }
            runCatching{ dbg.lineNumberProgram!!.eval(dbg) }.onSuccess {
                yield(Lnp(it,dbg.lineNumberProgram!!))
            }
        }
    }

    class Lnp(val state: LineNumberProgram.DebugState,val lnp: LineNumberProgram):ReusableTaggedRange{
        override val tag: String get() = "lnp"
        override fun range(): OffsetRange = OffsetRange.from(lnp.offset, state.lnpSize)
        override fun externalRanges(): Sequence<TaggedRange> = sequence {
            state.sourceCodeStringOff?.let { yield(Str(lnp.abc, it)) }
            state.addressLineColumns.forEach {
                when (it) {
                    is StartLocal -> {
                        yield(Str(lnp.abc, it.nameIdx))
                        yield(Str(lnp.abc, it.typeIdx))
                    }
                    is StartLocalExt -> {
                        yield(Str(lnp.abc, it.nameIdx))
                        yield(Str(lnp.abc, it.typeIdx))
                        yield(Str(lnp.abc, it.sigIdx))
                    }
                    is SetFile -> {
                        yield(Str(lnp.abc, it.nameIdx))
                    }
                    else -> {}
                }
            }
        }
    }


    fun plainYieldAll(): Sequence<TaggedRange> = sequence {
//        yield(this@TaggedRange)
//        externalRanges().forEach { yieldAll(it.plainYieldAll()) }
        val queue = ArrayDeque<TaggedRange>().apply { add(this@TaggedRange) }
        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            yield(curr)
            curr.externalRanges().forEach { queue.addLast(it) }
        }
    }
}