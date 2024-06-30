package me.yricky.oh.resde

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LimitKeyConfigTest{
    @Test
    fun testTAG(){
        val buf = ByteBuffer.wrap(ByteArray(4)).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(LimitKeyConfig.TAG)
        val tag = String(buf.array())
        println(tag)
        assertEquals("KEYS",tag)
    }
}