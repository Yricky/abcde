package me.yricky.oh.utils

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.wrapAsLEByteBuf
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel

fun File.asAbcBuf(readOnly:Boolean = true) = kotlin.run {
    AbcBuf(
        absolutePath,
        wrapAsLEByteBuf(
            FileChannel.open(toPath()).map(
                if(readOnly) FileChannel.MapMode.READ_ONLY else FileChannel.MapMode.READ_WRITE
                ,0,length()).order(ByteOrder.LITTLE_ENDIAN)
        )
    )
}