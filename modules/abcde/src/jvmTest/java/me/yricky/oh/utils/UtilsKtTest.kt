package me.yricky.oh.utils


import junit.framework.TestCase.assertEquals
import me.yricky.oh.common.value
import me.yricky.oh.common.wrapAsLEByteBuf
import org.junit.Test
import java.nio.ByteBuffer

class UtilsKtTest{
    @Test
    fun testSLeb(){
        byteArrayOf(0x80.toByte(),0x7f.toByte())
            .let { wrapAsLEByteBuf(ByteBuffer.wrap(it)) }
            .readSLeb128(0)
            .let {
                assertEquals(-128,it.value)
                println(it)
            }

        byteArrayOf(0x0.toByte())
            .let { wrapAsLEByteBuf(ByteBuffer.wrap(it)) }
            .readSLeb128(0)
            .let {
                assertEquals(0,it.value)
                println(it)
            }
        byteArrayOf(0x1.toByte())
            .let { wrapAsLEByteBuf(ByteBuffer.wrap(it)) }
            .readSLeb128(0)
            .let {
                assertEquals(1,it.value)
                println(it)
            }
        byteArrayOf(0x7f.toByte())
            .let { wrapAsLEByteBuf(ByteBuffer.wrap(it)) }
            .readSLeb128(0)
            .let {
                assertEquals(-1,it.value)
                println(it)
            }
    }
}