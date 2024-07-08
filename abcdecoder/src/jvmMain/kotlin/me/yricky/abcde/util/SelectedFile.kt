package me.yricky.abcde.util

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcHeader
import me.yricky.oh.common.wrapAsLEByteBuf
import me.yricky.oh.resde.ResIndexBuf
import me.yricky.oh.resde.ResIndexHeader
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.zip.ZipFile

sealed class SelectedFile(val file:File){
    abstract fun valid():Boolean

    companion object{
        fun fromOrNull(file:File):SelectedFile?{
            return when(file.extension.uppercase()){
                SelectedAbcFile.EXT -> SelectedAbcFile(file)
                SelectedIndexFile.EXT -> SelectedIndexFile(file)
                SelectedHapFile.EXT -> SelectedHapFile(file)
                else -> null
            }
        }
    }
}

class SelectedAbcFile(file: File, tag:String = file.path) :SelectedFile(file){
    val abcBuf by lazy {
        AbcBuf(
            tag,
            FileChannel.open(file.toPath())
                .map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                .let { wrapAsLEByteBuf(it.order(ByteOrder.LITTLE_ENDIAN)) }
        )
    }
    override fun valid(): Boolean {
        return file.length() > AbcHeader.SIZE &&
                abcBuf.header.isValid()
    }

    companion object{
        const val EXT = "ABC"
    }
}

class SelectedIndexFile(file: File) :SelectedFile(file){
    val resBuf by lazy {
        ResIndexBuf(
            FileChannel.open(file.toPath())
                .map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                .let { wrapAsLEByteBuf(it.order(ByteOrder.LITTLE_ENDIAN)) }
        )
    }
    override fun valid(): Boolean {
        return file.length() > ResIndexHeader.SIZE
    }

    companion object{
        const val EXT = "INDEX"
    }
}

class SelectedHapFile(file: File) :SelectedFile(file){
    val hap by lazy {
        kotlin.runCatching {
            ZipFile(file)
        }
    }
    override fun valid(): Boolean {
        return hap.isSuccess
    }

    companion object{
        const val EXT = "HAP"
    }
}