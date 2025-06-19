package com.yricky.oh.abclen

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.analyze.TaggedSize
import me.yricky.oh.abcd.analyze.yieldSize
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value
import me.yricky.oh.common.wrapAsLEByteBuf
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel

fun main(args: Array<String>){
    if(args.size == 0){
        println("需要一个参数，内容为abc文件的路径")
        return
    }
    val file = File(args[0])
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val abc = AbcBuf("", wrapAsLEByteBuf(mmap.order(ByteOrder.LITTLE_ENDIAN)))

    val strRefMap = mutableMapOf<Int, MutableSet<Int>>()
    val laRefMap = mutableMapOf<Int, MutableSet<Int>>()

    val sizeMap = mutableMapOf<String,Int>()
    var totalItemSize = 0

    abc.classes.forEach { off ,item ->
        if(item is AbcClass){
            val sizeKey = item.name.split('/').firstOrNull() ?: item.name
            var itemSize = 0
            yieldSize(item).forEach {
                if(it.isSize()){
                    itemSize += it.value
                }
                if(it.tag == TaggedSize.OFF32_STRING){
                    val clzSet = strRefMap[it.value]
                    if(clzSet == null){
                        strRefMap[it.value] = mutableSetOf(off)
                    } else {
                        clzSet.add(off)
                    }
                }
                if(it.tag == TaggedSize.OFF32_LA){
                    val clzSet = laRefMap[it.value]
                    if(clzSet == null){
                        laRefMap[it.value] = mutableSetOf(off)
                    } else {
                        clzSet.add(off)
                    }
                }
                if(it.tag == TaggedSize.OFF32_METHOD){
                    yieldSize(abc.method(it.value) as AbcMethod).forEach { mt ->
//                        println(mt.raw.toULong().toString(16))
                        if(mt.isSize()){
                            itemSize += mt.value
                        }
                        if(mt.tag == TaggedSize.OFF32_STRING){
                            val clzSet = strRefMap[mt.value]
                            if(clzSet == null){
                                strRefMap[mt.value] = mutableSetOf(off)
                            } else {
                                clzSet.add(off)
                            }
                        }
                        if(mt.tag == TaggedSize.OFF32_LA){
                            val clzSet = laRefMap[mt.value]
                            if(clzSet == null){
                                laRefMap[mt.value] = mutableSetOf(off)
                            } else {
                                clzSet.add(off)
                            }
                        }
                    }
                }
            }
            totalItemSize += itemSize
            sizeMap[sizeKey] = (sizeMap[sizeKey] ?: 0) + itemSize
        }
    }

    laRefMap.forEach { laOff,clzOffs ->
        val la = abc.literalArray(laOff)
        if(clzOffs.size == 1){
            val sizeKey = abc.classes[clzOffs.first()]!!.name.let {
                it.split('/').firstOrNull() ?: it
            }
            sizeMap[sizeKey] = (sizeMap[sizeKey] ?: 0) + la.intrinsicSize
            totalItemSize += la.intrinsicSize
        } else {
            println("在${clzOffs.size}个类中复用的LA:${la}")
        }
        yieldSize(la).forEach {
            if(it.tag == TaggedSize.OFF32_STRING){
                val clzSet = strRefMap[it.value]
                if(clzSet == null){
                    strRefMap[it.value] = clzOffs
                } else {
                    clzSet.addAll(clzOffs)
                }
            }
        }
    }

    var reuseStrSize = 0
    strRefMap.forEach { strOff,clzOffs ->
        val str = abc.stringItem(strOff)
        if(clzOffs.size == 1){
            val sizeKey = abc.classes[clzOffs.first()]!!.name.let {
                it.split('/').firstOrNull() ?: it
            }
            sizeMap[sizeKey] = (sizeMap[sizeKey] ?: 0) + (str.nextOffset - strOff)
            totalItemSize += (str.nextOffset - strOff)
        } else {
            reuseStrSize += (str.nextOffset - strOff)
//            println("在${clzOffs.size}个类中复用的字符串:${str.value}")
        }
    }

    val regionSize = abc.regions.fold(0) { s,r ->s + r.externalSize + 40 }

    println("文件体积:${file.length()}, 类占用的体积:${totalItemSize}, 被复用的字符串体积:${reuseStrSize}, 区域索引体积:${regionSize}")
    println("纳入统计的体积：${(totalItemSize + reuseStrSize + regionSize) * 100.0 / file.length()}")

    sizeMap.toList().sortedBy { -it.second }.forEach {
        println("cata:${it.first}\t size:${it.second}(${String.format("%.03f",it.second * 100.0 / totalItemSize)}%)")
    }
}