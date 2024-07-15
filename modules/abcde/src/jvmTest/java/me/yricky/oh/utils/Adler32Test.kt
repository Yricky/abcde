package me.yricky.oh.utils

import me.yricky.oh.common.wrapAsLEByteBuf
import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.Adler32
import kotlin.random.Random
import kotlin.time.measureTime

class Adler32Test{
    val ba = ByteArray(256*1024*1024).also {
//        var b:Byte = 0
        Random.nextBytes(it)
//        repeat(it.size){ i ->
//            b++
//            it[i] = b
//        }
    }
    val bb = ByteBuffer.wrap(ba).order(ByteOrder.LITTLE_ENDIAN)

    @Test
    fun testMyAdler32(){
        val adler32 = me.yricky.oh.utils.Adler32()
        adler32.update(wrapAsLEByteBuf(bb))
        println("my time:${
            measureTime {
                println(String.format("%08X",adler32.value()))
            }
        }")

        val adler32J = Adler32()
        adler32J.update(bb)
        println("java time:${
            measureTime {
                println(String.format("%08X",adler32J.value))
            }
        }")
    }
}