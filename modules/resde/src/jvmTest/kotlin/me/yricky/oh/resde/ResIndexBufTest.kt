package me.yricky.oh.resde

import me.yricky.oh.common.wrapAsLEByteBuf
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ResIndexBufTest {

    val file = File("/Users/yricky/Downloads/ohbili/resources.index")
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val res = ResIndexBuf(wrapAsLEByteBuf(mmap.order(ByteOrder.LITTLE_ENDIAN)))


    @Test
    fun getHeader() {
        println(String(res.header.version))
        println("size:${res.header.fileSize}")
        println("limitKeyConfigCount:${res.header.limitKeyConfigCount}")
    }

    @Test
    fun getLimitKeyConfigs() {
        res.limitKeyConfigs.forEach {
            println(it.idSetOffset)
            println(it.keyCount)
            println(it.data)
        }
    }
}