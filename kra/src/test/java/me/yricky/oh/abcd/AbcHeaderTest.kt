package me.yricky.oh.abcd

import org.junit.jupiter.api.Test
import java.io.File
import java.lang.RuntimeException
import java.nio.channels.FileChannel

class AbcHeaderTest{

    @Test
    fun test(){
        val file = File("/Users/yricky/Downloads/ets/modules.abc")
        val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
        val abc = AbcBuf(mmap)
        println("ver:${abc.header.version}")
        println("size:${abc.header.fileSize}")
        println("classNum:${abc.header.numClasses}")
        println("classIdxOff:${abc.header.classIdxOff}")
        println("fOff:${abc.header.foreignOff}")
        println("fSize:${abc.header.foreignSize}")
        println("l:${abc.header.numLiteralArrays}")
//        println("lnps:${abc.header.numLnps}")
//        abc.regions.forEach {
//            println("R[${it.header.startOff},${it.header.endOff})")
//            println("${it.protos.size},${it.protos.map { it.shorty }},")
//        }
//        abc.classes.forEach { l ->
//            val it = l.value
//            if(it is ClassItem) {
//                println("${it.region}c:${it.name}\n${it.data}")
//                it.fields.forEach {
//                    println("(f)\t${it.name}")
//                }
//                it.methods.forEach {
//                    println("(m) ${it.clazz.name} ${it.proto.shorty}\t${it.name}")
//                }
//            } else {
//                println("fc:${it.name}")
//            }
//        }
        abc.literalArrays.forEachIndexed { i,it ->
            println("${i} ${it.offset.toString(16)} size:${it.size}, flag:${it.flag}")
        }
    }
}