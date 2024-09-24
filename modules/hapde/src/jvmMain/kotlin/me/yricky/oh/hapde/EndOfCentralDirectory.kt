package me.yricky.oh.hapde

import me.yricky.oh.common.LEByteBuf
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * resolve zip EndOfCentralDirectory data
 * EndOfCentralDirectory format for:
 *
 * [MAGIC_NUM] (4 bytes) end of central dir signature 0x06054b50
 *
 * then:
 * @param diskNum (2 bytes) number of this disk
 * @param startDiskCDNum (2 bytes) number of the disk with the start of the central directory
 * @param thisDiskCDNum (2 bytes) total number of entries in the central directory on this disk
 * @param cdTotal (2 bytes) total number of entries in the central directory
 * @param cdSize (4 bytes) size of the central directory
 * @param offset (4 bytes) offset of start of central directory with respect to the starting disk number
 * @param commentLength (2 bytes) .ZIP file comment length
 * @param comment ([commentLength] bytes) .ZIP file comment
 */
class EndOfCentralDirectory(
    val diskNum: Int = 0,
    val startDiskCDNum:Int = 0,
    val thisDiskCDNum: Int = 0,
    val cdTotal:Int,
    val cdSize:Long,
    val offset:Long,
    val commentLength: Int = 0,
    val comment: ByteArray
) {

    companion object {
        /**
         * EndOfCentralDirectory invariable bytes length
         */
        const val EOCD_LEN: Int = 22
        const val MAGIC_NUM: Int = 0x06054b50

        fun from(bf: LEByteBuf): EndOfCentralDirectory? {
            if (bf.getInt(0) != MAGIC_NUM) {
                return null
            }
            var comment:ByteArray?
            val eocd = EndOfCentralDirectory(
                diskNum = bf.getShort(4).toInt() and 0xffff,
                startDiskCDNum = bf.getShort(6).toInt() and 0xffff,
                thisDiskCDNum = bf.getShort(8).toInt() and 0xffff,
                cdTotal = bf.getShort(10).toInt() and 0xffff,
                cdSize = bf.getInt(12).toLong() and 0xffff_ffffL,
                offset = bf.getInt(16).toLong() and 0xffff_ffffL,
                commentLength = (bf.getShort(20).toInt() and 0xffff).also { commentLength ->
                    comment = if (commentLength > 0) {
                        val readComment = ByteArray(commentLength)
                        bf.get(22,readComment)
                        readComment
                    } else ByteArray(0)
                },
                comment = comment ?: return null
            )
            return eocd
        }
    }
}