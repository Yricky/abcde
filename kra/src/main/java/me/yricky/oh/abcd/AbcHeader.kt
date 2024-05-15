package me.yricky.oh.abcd

import me.yricky.oh.utils.Uncleared
import java.nio.ByteBuffer

class AbcHeader(
    val buffer: ByteBuffer
) {
    val magic:ByteArray = ByteArray(8).also {
        buffer.get(0,it)
    }
    val checkSum = buffer.getInt(8)
    val version = buffer.getInt(12)
    val fileSize = buffer.getInt(16)
    val foreignOff = buffer.getInt(20)
    val foreignSize = buffer.getInt(24)
    val numClasses = buffer.getInt(28)
    val classIdxOff = buffer.getInt(32)
    val numLnps = buffer.getInt(36)
    val lnpIdxOff = buffer.getInt(40)
    @Uncleared("reserved")
    val numLiteralArrays = buffer.getInt(44)
    @Uncleared("reserved")
    val literalArrayIdxOff = buffer.getInt(48)
    val numIndexRegions = buffer.getInt(52)
    val indexSectionOff = buffer.getInt(56)

    fun version():String{
        return "${version and 0x0000ff}.${version.ushr(8) and 0x0000ff}.${version.ushr(16) and 0x0000ff}.${version.ushr(24) and 0x0000ff}"
    }

    fun isValid():Boolean{
        return magic.contentEquals(byteArrayOf(
            'P'.code.toByte(),
            'A'.code.toByte(),
            'N'.code.toByte(),
            'D'.code.toByte(),
            'A'.code.toByte(),
            0x0,0x0,0x0
            )) && fileSize == buffer.limit()
    }

    companion object{
        const val SIZE = 60
    }
}