package me.yricky.oh.abcd

import me.yricky.oh.common.LEByteBuf
import me.yricky.oh.utils.Uncleared
import kotlin.jvm.JvmInline

class AbcHeader(
    buffer: LEByteBuf
) {
    val magic:ByteArray = ByteArray(8).also {
        buffer.get(0,it)
    }
    val checkSum = buffer.getInt(8)
    val version = Version(buffer.getInt(12))
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

    fun isValid():Boolean{
        return magic.contentEquals(byteArrayOf(
            'P'.code.toByte(),
            'A'.code.toByte(),
            'N'.code.toByte(),
            'D'.code.toByte(),
            'A'.code.toByte(),
            0x0,0x0,0x0
            ))
    }

    @JvmInline
    value class Version(val version:Int){
        val mainVer get() = version and 0x0000ff
        val subVer  get() = version.ushr(8) and 0x0000ff
        val featVer get() = version.ushr(16) and 0x0000ff
        val buildVer get() = version.ushr(24) and 0x0000ff

        override fun toString(): String {
            return "$mainVer.$subVer.$featVer.$buildVer"
        }

        companion object{
            fun fromString(str:String):Version?{
                TODO()
            }
        }
    }

    companion object{
        const val SIZE = 60
    }
}