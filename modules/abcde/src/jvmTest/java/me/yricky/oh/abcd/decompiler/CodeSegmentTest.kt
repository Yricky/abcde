package me.yricky.oh.abcd.decompiler

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.isa.asmArgs
import me.yricky.oh.abcd.isa.util.BaseInstParser
import me.yricky.oh.common.wrapAsLEByteBuf
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class CodeSegmentTest{
    @Test
    fun test(){
        println(System.getenv("abcPath"))
        val file = File(System.getenv("abcPath"))
        val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
        val abc = AbcBuf("", wrapAsLEByteBuf(mmap.order(ByteOrder.LITTLE_ENDIAN)))
        abc.classes.mapNotNull { it.value as? AbcClass }
            .mapNotNull { it.methods.firstOrNull { it.name == "func_main_0" } }
            .mapNotNull { it.codeItem }
            .forEach {
                if(it.tryBlocks.isEmpty()){
                    println("gen for:${it.method.clazz.name} ${it.method.name}")
                    CodeSegment.genGraph(it.asm)
                }
            }
//        println(CodeSegment.genGraph(code.asm))
    }
}