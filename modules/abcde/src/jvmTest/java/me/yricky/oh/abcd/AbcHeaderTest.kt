package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.MethodTag
import me.yricky.oh.common.wrapAsLEByteBuf
import me.yricky.oh.utils.Adler32
import org.junit.Test
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class AbcHeaderTest{
    val file = File("/home/yricky/Downloads/modules.abc")
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val abc = AbcBuf("", wrapAsLEByteBuf(mmap.order(ByteOrder.LITTLE_ENDIAN)))

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testHeaders(){
        println("|------------HEADER START------------|")
        println("  magic:${abc.header.magic.toHexString()}")
        println("  checksum:${abc.header.checkSum.toString(16)}")
        println("  version:${abc.header.version}")
        println("  fileSize:${abc.header.fileSize}")
        println("  foreignOff:${abc.header.foreignOff}")
        println("  foreignSize:${abc.header.foreignSize}")
        println("  classNum:${abc.header.numClasses}")
        println("  classIdxOff:${abc.header.classIdxOff}")
        println("  numLnps:${abc.header.numLnps}")
        println("  lnpIdxOff:${abc.header.lnpIdxOff}")
        println("  numIndexRegions:${abc.header.numIndexRegions}")
        println("  indexSectionOff:${abc.header.indexSectionOff}")
        println("|-------------HEADER END-------------|")
    }

    @Test
    fun testCheckSum(){
        println(String.format("%08X",abc.header.checkSum))
        val adler32 = Adler32()
        adler32.update(abc.buf.slice(12,abc.buf.limit() - 12))
        println(String.format("%08X",adler32.value()))
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
        val moduleLiteralArrays = abc.classes.mapNotNull { (it.value as? AbcClass)?.moduleInfo }
        moduleLiteralArrays.forEach { u ->
            println("${u.offset.toString(16)} | ${u.moduleRequests}" +
                    "\n    ${u.regularImports}" +
                    "\n    ${u.namespaceImports}" +
                    "\n    ${u.localExports}" +
                    "\n    ${u.indirectExports}" +
                    "\n    ${u.starExports}")
        }
        println("size:${moduleLiteralArrays.size}")
    }
}