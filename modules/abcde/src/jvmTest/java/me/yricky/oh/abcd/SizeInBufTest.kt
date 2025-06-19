package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.common.wrapAsLEByteBuf
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import org.junit.Test



class SizeInBufTest {
    val file = File("/Users/yricky/Downloads/modules.abc")
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val abc = AbcBuf("", wrapAsLEByteBuf(mmap.order(ByteOrder.LITTLE_ENDIAN)))

    @Test
    fun testMethod(){
        println("file size: ${file.length()}")
        var iSize = 0
        var eSize = 0
        abc.classes.forEach { l ->
            val it = l.value
            if(it is AbcClass) {
                iSize += it.intrinsicSize
                eSize += it.externalSize
//                println("${it.name}")
            }
        }
        println("total size:${iSize + eSize} \nintrinsicSize:${iSize}\nexternalSize:${eSize}")
    }

}