package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.MethodTag
import org.junit.Test
import java.io.File
import java.nio.channels.FileChannel

class AbcHeaderTest{
    val file = File("/home/yricky/Downloads/modules.abc")
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val abc = AbcBuf("",mmap)

    @Test
    fun testHeaders(){

        println("ver:${abc.header.version}")
        println("size:${abc.header.fileSize}")
        println("classNum:${abc.header.numClasses}")
        println("classIdxOff:${abc.header.classIdxOff}")
        println("fOff:${abc.header.foreignOff}")
        println("fSize:${abc.header.foreignSize}")
//        println("l:${abc.header.numLiteralArrays}")
        println("indexes:${abc.header.numIndexRegions}")
        println("lnps:${abc.header.numLnps}")
    }

    @Test
    fun testRegion(){
        abc.regions.forEach {
            println(it)
//            println("${it.protos.size},${it.protos.map { it.shorty }},")
//            it.classes.forEach {
//                println("class:${it.name}")
//            }
//            it.methods.forEach {
//                println("method:${it.name}")
//            }
//            it.fields.forEach {
//                println("field:${it.name}")
//            }
        }
    }

    @Test
    fun testClasses(){
        abc.classes.forEach { l ->
            val it = l.value
            if(it is AbcClass) {
                println("${it.region}c:${it.name}\n${it.data}")
                it.fields.forEach {
                    println("(f)\t${it.name}")
                }
                it.methods.forEach {
                    println("(m) ${it.clazz.name} ${it.proto?.shorty}\t${it.name}")
                }
            } else {
                println("fc:${it.name}")
            }
        }
    }

    @Test
    fun testMethod(){
        abc.classes.forEach { l ->
            val it = l.value
            if(it is AbcClass) {
//                println("${it.name}")
                it.methods.filter { (it.codeItem?.triesSize ?: 0) > 0 }.forEach {
                    println("(m) ${it.clazz.name} ${it.indexData.functionKind} ${it.name}")
                    it.data.forEach { t ->
                        if(t is MethodTag.Anno){
                            val anno = t.anno
                            println("  annoType(${anno.clazz.name}):${anno.elements.map { it.toString(abc) }}")
                        }else if (t is MethodTag.ParamAnno){
                            val annos = t.get(abc)
                            println("  pAnnoCount:${annos.count}")
                        }
                    }
                    println("  cSize${it.codeItem?.codeSize}, tbSize:${it.codeItem?.triesSize}")
                    it.codeItem?.tryBlocks?.forEach {
                        println("    spc:${it.startPc},len:${it.length},nch:${it.numCatches}")
                        it.catchBlocks.forEach {
                            println("        ch:${it}")
                        }
                    }
                }
            }
        }
    }

//    @Test
//    fun testLA(){
//        abc.literalArrays.forEachIndexed { i,it ->
//            if(!abc.moduleLiteralArrays.containsKey(it.offset)){
//                println("${i} ${it.offset.toString(16)} size:${it.size}, ${it.content}")
//            }
//        }
//    }

    @Test
    fun testModuleLA(){
        abc.moduleLiteralArrays.forEach { (_, u) ->
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