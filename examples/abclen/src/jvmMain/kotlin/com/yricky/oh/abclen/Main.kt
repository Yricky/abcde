package com.yricky.oh.abclen

import me.yricky.oh.OffsetRange
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcHeader
import me.yricky.oh.abcd.analyze.TaggedRange
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.common.wrapAsLEByteBuf
import java.io.File
import java.io.PrintStream
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class SizeAnalyzeV2(
    val abc: AbcBuf,
    val sizeKeyGen: (AbcClass) -> String
) {

    fun analyze(fPrint: PrintStream){
        val uMap = TaggedUsageMap()
        val reusableMap = mutableMapOf<Int,Pair<TaggedRange, MutableSet<String>>>()
        abc.classes.forEach { (_,clz) ->
            if(clz is AbcClass){
                val key = sizeKeyGen(clz)
                TaggedRange.Clz(clz).plainYieldAll().forEach { s ->
                    val pair = reusableMap[s.range().start]
                    if(pair == null){
                        uMap.markAsUsed(s.range(), s.tag)
                        reusableMap[s.range().start] = Pair(s, mutableSetOf(key))
                    } else {
                        pair.second.add(key)
                    }
                }
            }
        }
        var total = 0
        uMap.markAsUsed(OffsetRange.from(0, AbcHeader.SIZE),"header")
        uMap.markAsUsed(OffsetRange.from(abc.header.classIdxOff,abc.header.numClasses * 4),"clzId")
        uMap.markAsUsed(OffsetRange.from(abc.header.lnpIdxOff,abc.header.numLnps * 4),"lnpId")
        uMap.markAsUsed(OffsetRange.from(abc.header.literalArrayIdxOff,abc.header.numLiteralArrays * 4),"laId")
        uMap.markAsUsed(OffsetRange.from(abc.header.indexSectionOff,abc.header.numIndexRegions * 40),"rHeader")
        abc.regions.forEach { r ->
            uMap.markAsUsed(OffsetRange.from(r.header.classIdxOff,r.header.classIdxSize * 4),"rClzId")
            uMap.markAsUsed(OffsetRange.from(r.header.mslIdxOff,r.header.mslIdxSize * 4),"rMslId")
        }
        mergeTaggedRanges(uMap.getUsedRanges(),listOf(
            setOf("mla","la"),
            setOf("dbg","lnp")
        )).forEach {
            fPrint.println(it)
            total += it.first.len
        }

        val sizeMap = mutableMapOf<String, Int>()
        var totalPrivate = 0
        var totalShared = 0
        reusableMap.forEach { (_,p) ->
            if(p.second.size == 1){
                sizeMap[p.second.first()] = (sizeMap[p.second.first()] ?: 0) + p.first.intrinsicSize
                totalPrivate += p.first.intrinsicSize
            } else {
                totalShared += p.first.intrinsicSize
            }
        }

        fPrint.println("-----私有体积")
        sizeMap.toList().sortedBy{ -it.second }.forEach { (k,v) ->
            fPrint.println("${k}\t${v} (${String.format("%.03f",v * 100.0 / totalPrivate)})")
        }


        fPrint.println("-----总结")
        fPrint.println("totalClassPrivate:${totalPrivate}")
        fPrint.println("totalClassShared:${totalShared}")
        val misc = abc.header.intrinsicSize +
                abc.header.numClasses * 4 +
                abc.header.numLnps * 4 +
                abc.header.numLiteralArrays * 4
                abc.regions.fold(0){ s,r -> s + 40 + r.header.classIdxSize * 4 + r.header.mslIdxSize * 4 }
        fPrint.println("misc:${misc}")
        fPrint.println("fileSize:${abc.buf.limit()}\ntotal:${total}\npercent:${total * 100.0 / abc.buf.limit()}")
    }
}

fun main(args: Array<String>){
    if(args.size == 0){
        println("需要一个参数，内容为abc文件的路径")
        return
    }
    val file = File(args[0])
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val abc = AbcBuf("", wrapAsLEByteBuf(mmap.order(ByteOrder.LITTLE_ENDIAN)))

    val reportFile = File("report-${file.name}.txt")
    println("out:${reportFile.absolutePath}")
    val fPrint = PrintStream(reportFile)
    val analyze = SizeAnalyzeV2(abc){ it.name.split('/').firstOrNull() ?: it.name }
    analyze.analyze(fPrint)

//    abc.classes.forEach { off ,item ->
//        if(item is AbcClass){
//            analyze.analyzeClass(item)
//        }
//    }
//    analyze.finish()
//
//    fPrint.println("文件体积:${file.length()}, 类占用的体积:${analyze.totalItemSize}, 被复用的字符串体积:${analyze.reuseStrSize}, 区域索引体积:${analyze.regionSize}")
//    fPrint.println("外部块体积:${abc.header.foreignSize}")
//    fPrint.println("杂项体积:${abc.header.intrinsicSize}")
//    fPrint.println("纳入统计的体积：${(abc.header.foreignSize + analyze.totalItemSize + analyze.reuseStrSize + analyze.regionSize) * 100.0 / file.length()}")
//
//    analyze.sizeMap.toList().sortedBy { -it.second }.forEach {
//        fPrint.println("cata:${it.first}\t size:${it.second}(${String.format("%.03f",it.second * 100.0 / analyze.totalItemSize)}%)")
//    }
}