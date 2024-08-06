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
            .let {
                val sleb = it.readSLeb128(0)
                assertEquals(sleb.value,it.readULeb128(0).value.uleb2sleb())
                assertEquals(-128,sleb.value)
                println(sleb)
            }

        byteArrayOf(0x0.toByte())
            .let { wrapAsLEByteBuf(ByteBuffer.wrap(it)) }
            .let {
                val sleb = it.readSLeb128(0)
                assertEquals(sleb.value,it.readULeb128(0).value.uleb2sleb())
                assertEquals(0,sleb.value)
                println(sleb)
            }
        byteArrayOf(0x1.toByte())
            .let { wrapAsLEByteBuf(ByteBuffer.wrap(it)) }
            .let {
                val sleb = it.readSLeb128(0)
                assertEquals(sleb.value,it.readULeb128(0).value.uleb2sleb())
                assertEquals(1,sleb.value)
                println(sleb)
            }
        byteArrayOf(0x7f.toByte())
            .let { wrapAsLEByteBuf(ByteBuffer.wrap(it)) }
            .let {
                val sleb = it.readSLeb128(0)
                assertEquals(sleb.value,it.readULeb128(0).value.uleb2sleb())
                assertEquals(-1,sleb.value)
                println(sleb)
            }
    }
}