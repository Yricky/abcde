package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.ClassItem
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.channels.FileChannel

class AbcHeaderTest{
    val file = File("/Users/yricky/Downloads/ets/modules.abc")
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val abc = AbcBuf(mmap)

    @Test
    fun testHeaders(){

        println("ver:${abc.header.version}")
        println("size:${abc.header.fileSize}")
        println("classNum:${abc.header.numClasses}")
        println("classIdxOff:${abc.header.classIdxOff}")
        println("fOff:${abc.header.foreignOff}")
        println("fSize:${abc.header.foreignSize}")
        println("l:${abc.header.numLiteralArrays}")
        println("lnps:${abc.header.numLnps}")
    }

    @Test
    fun testRegion(){
        abc.regions.forEach {
            println("R[${it.header.startOff},${it.header.endOff})")
            println("${it.protos.size},${it.protos.map { it.shorty }},")
        }
    }

    @Test
    fun testClasses(){
        abc.classes.forEach { l ->
            val it = l.value
            if(it is ClassItem) {
                println("${it.region}c:${it.name}\n${it.data}")
                it.fields.forEach {
                    println("(f)\t${it.name}")
                }
                it.methods.forEach {
                    println("(m) ${it.clazz.name} ${it.proto.shorty}\t${it.name}")
                }
            } else {
                println("fc:${it.name}")
            }
        }
    }

    @Test
    fun testLA(){
        abc.literalArrays.forEachIndexed { i,it ->
            if(!abc.moduleLiteralArrays.containsKey(it.offset)){
                println("${i} ${it.offset.toString(16)} size:${it.size}, ${it.content}")
            }
        }
    }

    @Test
    fun testModuleLA(){
        abc.moduleLiteralArrays.forEach { (t, u) ->
            println("${u.offset.toString(16)} | ${u.moduleRequests}" +
                    "\n    ${u.regularImports}" +
                    "\n    ${u.namespaceImports}" +
                    "\n    ${u.localExports}" +
                    "\n    ${u.indirectExports}" +
                    "\n    ${u.starExports}")
        }
        println("size:${abc.moduleLiteralArrays.size}")
    }
}