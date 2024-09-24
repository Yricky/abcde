package me.yricky.oh.hapde

import me.yricky.oh.common.LEByteBuf


class HapFileInfo(
    val eocdOffset:Int,
    val eocd:EndOfCentralDirectory
){
    val centralDirectoryOffset:Long get() = eocd.offset
    val centralDirectorySize:Int get() = eocd.cdSize.toInt()
    val centralDirectoryEntryCount:Int get() = eocd.cdTotal
    companion object{
        fun from(buf:LEByteBuf):HapFileInfo?{
            val len = buf.limit()
            var commentSize = 0
            while (commentSize < 0xffff){
                val off = len - EndOfCentralDirectory.EOCD_LEN - commentSize
                if(buf.getInt(off) == EndOfCentralDirectory.MAGIC_NUM){
                    val ba = buf.slice(off,EndOfCentralDirectory.EOCD_LEN + commentSize)
                    val eocd = EndOfCentralDirectory.from(ba)
                    if(eocd != null){
                        return HapFileInfo(off, eocd)
                    }
                }
                commentSize++
            }
            return null
        }
    }
}